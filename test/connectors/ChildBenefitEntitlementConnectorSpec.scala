package connectors

import base.BaseAppSpec
import config.FrontendAppConfig
import models.entitlement.ChildBenefitEntitlement
import models.errors.{CBError, ConnectorError}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpReads, UpstreamErrorResponse}
import utils.TestData.{genericCBError, invalidJsonResponse}

import scala.concurrent.{ExecutionContext, Future}

class ChildBenefitEntitlementConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier = new HeaderCarrier()

  override implicit lazy val app = applicationBuilder().build()
  val sutWithStubs = app.injector.instanceOf[ChildBenefitEntitlementConnector]

  val mockHttpClient = mock[HttpClient]
  val mockAppConfig = mock[FrontendAppConfig]
  val sutWithMocks = new DefaultChildBenefitEntitlementConnector(mockHttpClient, mockAppConfig)

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
          entitlementsAndPaymentHistoryFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

          whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr) { (responseCode, message) =>
              when(mockHttpClient.GET
                (any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])
                (any[HttpReads[Either[CBError, _]]], any[HeaderCarrier], any[ExecutionContext])
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
              when(mockHttpClient.GET
                (any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])
                (any[HttpReads[Either[CBError, _]]], any[HeaderCarrier], any[ExecutionContext])
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
          forAll(arbitrary[ChildBenefitEntitlement]) { childBenefitEntitlement =>
            entitlementsAndPaymentHistoryFailureStub(OK, invalidJsonResponse)

            whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
              result mustBe a[Left[ConnectorError, ChildBenefitEntitlement]]
              result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
            }
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
