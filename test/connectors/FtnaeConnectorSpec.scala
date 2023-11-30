package connectors

import base.BaseAppSpec
import models.errors.{ConnectorError, FtnaeCannotFindYoungPersonError, FtnaeNoCHBAccountError}
import models.ftnae.{ChildDetails, FtnaeResponse}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestData._

import scala.concurrent.ExecutionContext

class FtnaeConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier = new HeaderCarrier()

  override implicit lazy val app = applicationBuilder().build()
  val sut = app.injector.instanceOf[FtnaeConnector]

  "FtnaeConnector" - {
    "getFtnaeAccountDetails" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ChildBenefitEntitlement in the response is returned" in {
          forAll(arbitrary[FtnaeResponse]) { ftnaeResponse =>
            getFtnaeAccountDetailsStub(ftnaeResponse)

            whenReady(sut.getFtnaeAccountDetails.value) { result =>
              result mustBe Right(ftnaeResponse)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a noChBAccount response" - {
          "THEN an expected FtnaeNoCHBAccountError is returned" in {
            getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeNoChBAccountErrorResponse)

            whenReady(sut.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(FtnaeNoCHBAccountError)
            }
          }
        }
        "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
          "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
            getFtnaeAccountDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

            whenReady(sut.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(FtnaeCannotFindYoungPersonError)
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            getFtnaeAccountDetailsFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

            whenReady(sut.getFtnaeAccountDetails.value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
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

            whenReady(sut.uploadFtnaeDetails(childDetails).value) { result =>
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

              whenReady(sut.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(FtnaeNoCHBAccountError)
              }
            }
          }
        }
        "AND the response matches a ftnaeCannotFindYoungPersonError response" - {
          "THEN an expected FtnaeCannotFindYoungPersonError is returned" in {
            forAll(arbitrary[ChildDetails]) { childDetails =>
              uploadFtnaeDetailsFailureStub(NOT_FOUND, ftnaeCannotFindYoungPersonErrorResponse)

              whenReady(sut.uploadFtnaeDetails(childDetails).value) { result =>
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

              whenReady(sut.uploadFtnaeDetails(childDetails).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
    }
  }
}
