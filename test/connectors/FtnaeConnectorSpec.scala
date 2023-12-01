package connectors

import base.BaseAppSpec
import config.FrontendAppConfig
import models.errors.{CBError, ConnectorError, FtnaeCannotFindYoungPersonError, FtnaeNoCHBAccountError}
import models.ftnae.{ChildDetails, FtnaeResponse}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.Writes
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpReads, UpstreamErrorResponse}
import utils.TestData._

import scala.concurrent.{ExecutionContext, Future}

class FtnaeConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier = new HeaderCarrier()

  override implicit lazy val app = applicationBuilder().build()
  val sutWithStubs = app.injector.instanceOf[FtnaeConnector]

  val mockHttpClient = mock[HttpClient]
  val mockAppConfig = mock[FrontendAppConfig]
  val sutWithMocks = new FtnaeConnector(mockHttpClient, mockAppConfig)

  "FtnaeConnector" - {
    "getFtnaeAccountDetails" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ChildBenefitEntitlement in the response is returned" in {
          forAll(arbitrary[FtnaeResponse]) { ftnaeResponse =>
            getFtnaeAccountDetailsStub(ftnaeResponse)

            whenReady(sutWithStubs.getFtnaeAccountDetails.value) { result =>
              result mustBe Right(ftnaeResponse)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a noChBAccount response" - {
          "THEN an expected FtnaeNoCHBAccountError is returned" in {
            getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeNoChBAccountErrorResponse)

            whenReady(sutWithStubs.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(FtnaeNoCHBAccountError)
            }
          }
        }
        "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
          "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
            getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

            whenReady(sutWithStubs.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(FtnaeCannotFindYoungPersonError)
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            getFtnaeAccountDetailsFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

            whenReady(sutWithStubs.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
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

              whenReady(sutWithMocks.getFtnaeAccountDetails.value) { result =>
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

              whenReady(sutWithMocks.getFtnaeAccountDetails.value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
    }

    "uploadFtnaeDetails" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          forAll(arbitrary[ChildDetails]) { childDetails =>
            uploadFtnaeDetailsStub(childDetails)

            whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
              result mustBe Right(())
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a noChBAccount response" - {
          "THEN an expected FtnaeNoCHBAccountError is returned" in {
            forAll(arbitrary[ChildDetails]) { childDetails =>
              uploadFtnaeDetailsFailureStub(NOT_FOUND, ftnaeNoChBAccountErrorResponse)

              whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(FtnaeNoCHBAccountError)
              }
            }
          }
        }
        "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
          "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
            forAll(arbitrary[ChildDetails]) { childDetails =>
              uploadFtnaeDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

              whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(FtnaeCannotFindYoungPersonError)
              }
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            forAll(arbitrary[ChildDetails]) { childDetails =>
              val expectedMessage = "Unit Test other failure expected message"
              uploadFtnaeDetailsFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

              whenReady(sutWithStubs.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
      "GIVEN the HttpClient experiences an exception" - {
        "AND the exception is of type HttpException" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[ChildDetails]) { (responseCode, message, childDetails) =>
              when(mockHttpClient.PUT
              (any[String], any[ChildDetails], any[Seq[(String, String)]])
              (any[Writes[ChildDetails]], any[HttpReads[Either[CBError, _]]], any[HeaderCarrier], any[ExecutionContext])
              ).thenReturn(Future failed new HttpException(message, responseCode))

              whenReady(sutWithMocks.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
        "AND the exception is of type UpstreamErrorResponse" - {
          "THEN a ConnectorError is returned with the matching details" in {
            forAll(randomFailureStatusCode, alphaStr, arbitrary[ChildDetails]) { (responseCode, message, childDetails) =>
              when(mockHttpClient.PUT
              (any[String], any[ChildDetails], any[Seq[(String, String)]])
              (any[Writes[ChildDetails]], any[HttpReads[Either[CBError, _]]], any[HeaderCarrier], any[ExecutionContext])
              ).thenReturn(Future failed UpstreamErrorResponse(message, responseCode))

              whenReady(sutWithMocks.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(ConnectorError(responseCode, message))
              }
            }
          }
        }
      }
    }

    "companion object: FtnaeConnector" - {
      "claimantInfoLogMessage" - {
        "GIVEN a status code and a message" - {
          "THEN the returned log message contains both" in {
            forAll(randomFailureStatusCode, alphaStr) { (statusCode, message) =>
              val result = FtnaeConnector.claimantInfoLogMessage(statusCode, message)

              result must include(statusCode.toString)
              result must include(message)
            }
          }
        }
      }
    }
  }
}
