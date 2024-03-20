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
import models.entitlement.ChildBenefitEntitlement
import models.errors.{CBError, ConnectorError}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SERVICE_UNAVAILABLE}
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpReads, UpstreamErrorResponse}
import utils.TestData.{genericCBError, invalidJsonResponse, validNotMatchingJsonResponse}

import scala.concurrent.{ExecutionContext, Future}

class ChildBenefitEntitlementConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier: HeaderCarrier       = new HeaderCarrier()

  override implicit lazy val app: Application         = applicationBuilder().build()
  val sutWithStubs: ChildBenefitEntitlementConnector  = app.injector.instanceOf[ChildBenefitEntitlementConnector]

  val mockHttpClient: HttpClient                           = mock[HttpClient]
  val mockAppConfig: FrontendAppConfig                     = mock[FrontendAppConfig]
  val sutWithMocks:DefaultChildBenefitEntitlementConnector = new DefaultChildBenefitEntitlementConnector(mockHttpClient, mockAppConfig)

  "ChildBenefitEntitlementConnector" - {
    "getChildBenefitEntitlement" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ChildBenefitEntitlement in the response is returned" in {
          forAll(arbitrary[ChildBenefitEntitlement]) { childBenefitEntitlement =>
            entitlementsAndPaymentHistoryStub(childBenefitEntitlement)

            whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
              result mustBe Right(childBenefitEntitlement)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          entitlementsAndPaymentHistoryFailureStub(
            INTERNAL_SERVER_ERROR,
            genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
          )

          whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>
              when(
                mockHttpClient.GET(any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])(
                  any[HttpReads[Either[CBError, _]]],
                  any[HeaderCarrier],
                  any[ExecutionContext]
                )
              ).thenReturn(Future failed new HttpException(message, responseCode))

              whenReady(sutWithMocks.getChildBenefitEntitlement.value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>
              when(
                mockHttpClient.GET(any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])(
                  any[HttpReads[Either[CBError, _]]],
                  any[HeaderCarrier],
                  any[ExecutionContext]
                )
              ).thenReturn(Future failed UpstreamErrorResponse(message, responseCode))

              whenReady(sutWithMocks.getChildBenefitEntitlement.value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient receives a successful response that is invalid Json" - {
        "THEN an expected ConnectorError is returned" in {
          forAll(arbitrary[ChildBenefitEntitlement]) { _ =>
            entitlementsAndPaymentHistoryFailureStub(OK, invalidJsonResponse)

            whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
              result mustBe a[Left[_, ChildBenefitEntitlement]]
              result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
            }
          }
        }
      }
      "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
        "THEN an expected ConnectorError is returned" in {
          entitlementsAndPaymentHistoryFailureStub(OK, validNotMatchingJsonResponse)

          whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
            result mustBe a[Left[_, ChildBenefitEntitlement]]
            result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
          }
        }
      }
    }

    "companion object: DefaultChildBenefitEntitlementConnector" - {
      "logMessage" - {
        "GIVEN a status code and a message" - {
          "THEN the returned log message contains both" in {
            forAll(randomFailureStatusCode, alphaStr) { (statusCode, message) =>
              val result = DefaultChildBenefitEntitlementConnector.logMessage(statusCode, message)

              result must include(statusCode.toString)
              result must include(message)
            }
          }
        }
      }
    }
  }
}
