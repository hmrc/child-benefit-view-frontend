/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.BaseAppSpec
import config.FrontendAppConfig
import izumi.reflect.Tag
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.errors.{CBError, ConnectorError, FtnaeCannotFindYoungPersonError, FtnaeNoCHBAccountError}
import models.ftnae.CourseDuration.OneYear
import models.ftnae.{ChildDetails, FtnaeChildInfo, FtnaeClaimantInfo, FtnaeQuestionAndAnswer, FtnaeResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNAUTHORIZED}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.BodyWritable
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, UpstreamErrorResponse}
import utils.TestData._

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FtnaeConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier:    HeaderCarrier    = new HeaderCarrier()

  override implicit lazy val app: Application    = applicationBuilder().build()
  val sutWithStubs:               FtnaeConnector = app.injector.instanceOf[FtnaeConnector]

  val mockHttpClient: HttpClientV2      = mock[HttpClientV2]
  val requestBuilder: RequestBuilder    = mock[RequestBuilder]
  val mockAppConfig:  FrontendAppConfig = mock[FrontendAppConfig]
  val sutWithMocks:   FtnaeConnector    = new FtnaeConnector(mockHttpClient, mockAppConfig)

  val errors: Seq[Int] = Seq(
    UNAUTHORIZED,
    NOT_FOUND,
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE
  )

  def connector: FtnaeConnector = {
    val injector: Injector = GuiceApplicationBuilder()
      .overrides(
        inject.bind[RequestBuilder].toInstance(requestBuilder),
        inject.bind[HttpClientV2].toInstance(mockHttpClient)
      )
      .injector()

    injector.instanceOf[FtnaeConnector]
  }

  val childDetails: ChildDetails = ChildDetails(
    OneYear,
    ChildReferenceNumber("ref1"),
    LocalDate.now().minusYears(10),
    "Child A",
    List.empty[FtnaeQuestionAndAnswer]
  )

  val ftnaeResponse: FtnaeResponse = FtnaeResponse(
    claimant = FtnaeClaimantInfo(
      name = FirstForename("forename"),
      surname = Surname("Surname")
    ),
    children = List.empty[FtnaeChildInfo]
  )

  val message = "error message"

  "getFtnaeAccountDetails" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the ChildBenefitEntitlement in the response is returned" in {
        getFtnaeAccountDetailsStub(ftnaeResponse)

        whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
          result mustBe Right(ftnaeResponse)
        }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a noChBAccount response" - {
        "THEN an expected FtnaeNoCHBAccountError is returned" in {
          getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeNoChBAccountErrorResponse)

          whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
            result mustBe Left(FtnaeNoCHBAccountError)
          }
        }
      }
      "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
        "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
          getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

          whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
            result mustBe Left(FtnaeCannotFindYoungPersonError)
          }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          getFtnaeAccountDetailsFailureStub(
            INTERNAL_SERVER_ERROR,
            genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
          )

          whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }
    "GIVEN the HttpClient experiences an exception" - {
      when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
      errors.foreach { responseCode =>
        s"AND the exception is of type HttpException: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(
              requestBuilder.execute[Either[CBError, FtnaeResponse]](
                any[HttpReads[Either[CBError, FtnaeResponse]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.getFtnaeAccountDetails().value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }

        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(
              requestBuilder.execute[Either[CBError, FtnaeResponse]](
                any[HttpReads[Either[CBError, FtnaeResponse]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.getFtnaeAccountDetails().value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        getFtnaeAccountDetailsFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
          result mustBe a[Left[_, FtnaeResponse]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
      }
    }
    "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
      "THEN an expected ConnectorError is returned" in {
        getFtnaeAccountDetailsFailureStub(OK, validNotMatchingJsonResponse)

        whenReady(sutWithStubs.getFtnaeAccountDetails().value) { result =>
          result mustBe a[Left[_, FtnaeResponse]]
          result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
        }
      }
    }
  }

  "uploadFtnaeDetails" - {
    errors.foreach { responseCode =>
      "GIVEN the HttpClient experiences an exception" - {
        when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
          .thenReturn(requestBuilder)

        when(
          requestBuilder.withBody(any[ChildDetails])(
            any[BodyWritable[ChildDetails]],
            any[Tag[ChildDetails]],
            any[ExecutionContext]
          )
        ).thenReturn(requestBuilder)

        s"AND the exception is of type HttpException: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(
              requestBuilder.execute[Either[CBError, Unit]](
                any[HttpReads[Either[CBError, Unit]]],
                any[ExecutionContext]
              )
            ).thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.uploadFtnaeDetails(childDetails).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }

        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          s"THEN a ConnectorError is returned with the matching details" in {
            when(
              requestBuilder.execute[Either[CBError, Unit]](
                any[HttpReads[Either[CBError, Unit]]],
                any[ExecutionContext]
              )
            ).thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.uploadFtnaeDetails(childDetails).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response" - {
      "THEN the a Unit response is returned" in {
        uploadFtnaeDetailsStub(childDetails)

        when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
          .thenReturn(requestBuilder)

        when(
          requestBuilder.withBody(any[ChildDetails])(
            any[BodyWritable[ChildDetails]],
            any[Tag[ChildDetails]],
            any[ExecutionContext]
          )
        )
          .thenReturn(requestBuilder)

        when(
          requestBuilder.execute[Either[CBError, Unit]](
            any[HttpReads[Either[CBError, Unit]]],
            any[ExecutionContext]
          )
        )
          .thenReturn(Future.successful(Right(())))

        whenReady(connector.uploadFtnaeDetails(childDetails).value) { result =>
          result mustBe Right(())
        }
      }
    }

    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a noChBAccount response" - {
        "THEN an expected FtnaeNoCHBAccountError is returned" in {
          uploadFtnaeDetailsFailureStub(NOT_FOUND, ftnaeNoChBAccountErrorResponse)

          whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
            result mustBe Left(FtnaeNoCHBAccountError)
          }
        }
      }
      "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
        "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
          uploadFtnaeDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

          whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
            result mustBe Left(FtnaeCannotFindYoungPersonError)
          }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          uploadFtnaeDetailsFailureStub(
            INTERNAL_SERVER_ERROR,
            genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
          )

          whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }
  }

  "GIVEN the HttpClient receives a successful response that is invalid Json" - {
    "THEN an expected ConnectorError is returned" in {
      uploadFtnaeDetailsFailureStub(OK, invalidJsonResponse)

      whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
        result mustBe a[Left[_, FtnaeResponse]]
        result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
      }
    }
  }

  "companion object: FtnaeConnector" - {
    "claimantInfoLogMessage" - {
      errors.foreach { statusCode =>
        s"GIVEN a status code: $statusCode" - {
          "THEN the returned log message contains both" in {
            val result = FtnaeConnector.claimantInfoLogMessage(statusCode, message)

            result must include(statusCode.toString)
            result must include(message)
          }
        }
      }
    }
  }
}
