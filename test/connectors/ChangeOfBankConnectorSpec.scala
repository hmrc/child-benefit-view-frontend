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
import models.changeofbank.ClaimantBankInformation
import models.cob.{UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.errors.{CBError, ClaimantIsLockedOutOfChangeOfBank, ConnectorError, PriorityBARSVerificationError}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.BodyWritable
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestData._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class ChangeOfBankConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite with HttpClientV2Support with WireMockSupport {
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val hc: HeaderCarrier    = new HeaderCarrier()

  override implicit lazy val app: Application           = applicationBuilder().build()
  val sutWithStubs:               ChangeOfBankConnector = app.injector.instanceOf[ChangeOfBankConnector]

  val mockHttpClient: HttpClientV2            = mock[HttpClientV2]
  val requestBuilder: RequestBuilder    = mock[RequestBuilder]
  val mockAppConfig:  FrontendAppConfig     = mock[FrontendAppConfig]
  val sutWithMocks:   ChangeOfBankConnector = new ChangeOfBankConnector(mockHttpClient, mockAppConfig)

  def connector: ChangeOfBankConnector = {
    val injector: Injector = GuiceApplicationBuilder()
      .overrides(
        inject.bind[RequestBuilder].toInstance(requestBuilder),
        inject.bind[HttpClientV2].toInstance(mockHttpClient)
      ).injector()

    injector.instanceOf[ChangeOfBankConnector]
  }


  "ChangeOfBankConnector" - {
    "getChangeOfBankClaimantInfo" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ClaimantBankInformation in the response is returned" in {
          forAll(arbitrary[ClaimantBankInformation]) { claimantBankInformation =>
            changeOfBankUserInfoStub(claimantBankInformation)

            whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Right(claimantBankInformation)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            changeOfBankUserInfoFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

            whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            changeOfBankUserInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

            whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            changeOfBankUserInfoFailureStub(
              INTERNAL_SERVER_ERROR,
              genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
            )

            whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>

              when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, ClaimantBankInformation]](any, any))
                .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

              whenReady(connector.getChangeOfBankClaimantInfo.value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>

              when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, ClaimantBankInformation]](any, any))
                .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

              whenReady(connector.getChangeOfBankClaimantInfo.value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          changeOfBankUserInfoFailureStub(OK, invalidJsonResponse)

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe a[Left[_, ClaimantBankInformation]]
            result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
          }
        }
      }
      "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
        "THEN an expected ConnectorError is returned" in {
          changeOfBankUserInfoFailureStub(OK, validNotMatchingJsonResponse)

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe a[Left[_, ClaimantBankInformation]]
            result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
          }
        }
      }
    }

    "verifyClaimantBankAccount" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
            verifyClaimantBankInfoStub()

            whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Right(())
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
              verifyClaimantBankInfoFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

              whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
              }
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
              verifyClaimantBankInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

              whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
              }
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
              val expectedMessage = "Unit Test other failure expected message"
              verifyClaimantBankInfoFailureStub(
                INTERNAL_SERVER_ERROR,
                genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
              )

              whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[VerifyBankAccountRequest]) {
              (responseCode, message, verifyBankAccountRequest) =>

                when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
                  .thenReturn(requestBuilder)

                when(requestBuilder.withBody(any[VerifyBankAccountRequest])(any[BodyWritable[VerifyBankAccountRequest]], any, any))
                  .thenReturn(requestBuilder)

                when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                  .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

                whenReady(connector.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                  result mustBe Left(ConnectorError(responseCode, message))
                }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[VerifyBankAccountRequest]) {
              (responseCode, message, verifyBankAccountRequest) =>

                when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
                  .thenReturn(requestBuilder)

                when(requestBuilder.withBody(any[VerifyBankAccountRequest])(any[BodyWritable[VerifyBankAccountRequest]], any, any))
                  .thenReturn(requestBuilder)

                when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                  .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

                whenReady(connector.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                  result mustBe Left(ConnectorError(responseCode, message))
                }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
            verifyClaimantBankAccountFailureStub(OK, invalidJsonResponse)

            whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe a[Left[_, Unit]]
              result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
            }
          }
        }
      }
    }

    "verifyBARNotLocked" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          verifyBARNotLockedStub()

          whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
            result mustBe Right(())
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

            whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
              result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            verifyBARNotLockedFailureStub(NOT_FOUND, barsFailureErrorResponse)

            whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
              result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

            whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>
              when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

              whenReady(connector.verifyBARNotLocked().value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>
              when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

              whenReady(connector.verifyBARNotLocked().value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          verifyBARNotLockedFailureStub(OK, invalidJsonResponse)

          whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
            result mustBe a[Left[_, Unit]]
            result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
          }
        }
      }
    }

    "updateBankAccount" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
            updateBankAccountStub(UpdateBankDetailsResponse("Success"))

            whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
              result mustBe Right(UpdateBankDetailsResponse("Success"))
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
              updateBankAccountFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

              whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
              }
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
              updateBankAccountFailureStub(NOT_FOUND, barsFailureErrorResponse)

              whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
              }
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
              val expectedMessage = "Unit Test other failure expected message"
              updateBankAccountFailureStub(
                INTERNAL_SERVER_ERROR,
                genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
              )

              whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[UpdateBankAccountRequest]) {
              (responseCode, message, updateBankAccountRequest) =>

                when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
                  .thenReturn(requestBuilder)

                when(requestBuilder.withBody(any[UpdateBankAccountRequest])(any[BodyWritable[UpdateBankAccountRequest]], any, any))
                  .thenReturn(requestBuilder)

                when(requestBuilder.execute[Either[CBError, UpdateBankDetailsResponse]](any, any))
                  .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

                whenReady(connector.updateBankAccount(updateBankAccountRequest).value) { result =>
                  result mustBe Left(ConnectorError(responseCode, message))
                }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[UpdateBankAccountRequest]) {
              (responseCode, message, updateBankAccountRequest) =>
                when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
                  .thenReturn(requestBuilder)

                when(requestBuilder.withBody(any[UpdateBankAccountRequest])(any[BodyWritable[UpdateBankAccountRequest]], any, any))
                  .thenReturn(requestBuilder)

                when(requestBuilder.execute[Either[CBError, UpdateBankDetailsResponse]](any, any))
                  .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

                whenReady(connector.updateBankAccount(updateBankAccountRequest).value) { result =>
                  result mustBe Left(ConnectorError(responseCode, message))
                }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
            updateBankAccountFailureStub(OK, invalidJsonResponse)

            whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
              result mustBe a[Left[_, UpdateBankDetailsResponse]]
              result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
            }
          }
        }
      }
      "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
        "THEN an expected ConnectorError is returned" in {
          forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
            updateBankAccountFailureStub(OK, validNotMatchingJsonResponse)

            whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
              result mustBe a[Left[_, UpdateBankDetailsResponse]]
              result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
            }
          }
        }
      }
    }

    "dropChangeOfBankCache" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          dropChangeOfBankStub()

          whenReady(sutWithStubs.dropChangeOfBankCache().value) { result =>
            result mustBe Right(())
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>

              when(mockHttpClient.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

              whenReady(connector.dropChangeOfBankCache().value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>

              when(mockHttpClient.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

              when(requestBuilder.execute[Either[CBError, Unit]](any, any))
                .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

              whenReady(connector.dropChangeOfBankCache().value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          dropChangeOfBankFailureStub(OK, invalidJsonResponse)

          whenReady(sutWithStubs.dropChangeOfBankCache().value) { result =>
            result mustBe a[Left[_, Unit]]
            result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
          }
        }
      }
    }

    "Exception Recovery" - {}
  }
}
