package connectors

import base.BaseAppSpec
import models.changeofbank.ClaimantBankInformation
import models.cob.{UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.errors.{ClaimantIsLockedOutOfChangeOfBank, ConnectorError, PriorityBARSVerificationError}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.HeaderCarrier
import stubs.ChildBenefitServiceStubs._
import utils.TestData._

import scala.concurrent.ExecutionContext

class ChangeOfBankConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier = new HeaderCarrier()

  override implicit lazy val app = applicationBuilder().build()
  val sut = app.injector.instanceOf[ChangeOfBankConnector]

  "ChangeOfBankConnector" - {
    "getChangeOfBankClaimantInfo" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ClaimantBankInformation in the response is returned" in {
          forAll(arbitrary[ClaimantBankInformation]) { claimantBankInformation =>
            changeOfBankUserInfoStub(claimantBankInformation)

            whenReady(sut.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Right(claimantBankInformation)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            changeOfBankUserInfoFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

            whenReady(sut.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            changeOfBankUserInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

            whenReady(sut.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            changeOfBankUserInfoFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

            whenReady(sut.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
          }
        }
      }
    }

    "verifyClaimantBankAccount" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
            verifyClaimantBankInfoStub()

            whenReady(sut.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
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

              whenReady(sut.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
              }
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
              verifyClaimantBankInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

              whenReady(sut.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
              }
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            forAll(arbitrary[VerifyBankAccountRequest]) { verifyBankAccountRequest =>
              val expectedMessage = "Unit Test other failure expected message"
              verifyClaimantBankInfoFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

              whenReady(sut.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
    }

    "verifyBARNotLocked" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          verifyBARNotLockedStub()

          whenReady(sut.verifyBARNotLocked.value) { result =>
            result mustBe Right(())
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "AND the response matches a lockedOutError response" - {
          "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

            whenReady(sut.verifyBARNotLocked.value) { result =>
              result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            verifyBARNotLockedFailureStub(NOT_FOUND, barsFailureErrorResponse)

            whenReady(sut.verifyBARNotLocked.value) { result =>
              result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

            whenReady(sut.verifyBARNotLocked.value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
          }
        }
      }
    }

    "updateBankAccount" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the a Unit response is returned" in {
          forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
            updateBankAccountStub(UpdateBankDetailsResponse("Success"))

            whenReady(sut.updateBankAccount(updateBankAccountRequest).value) { result =>
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

              whenReady(sut.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
              }
            }
          }
        }
        "AND the response matches a barsFailureError response" - {
          "THEN an expected PriorityBARSVerificationError is returned" in {
            forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
              updateBankAccountFailureStub(NOT_FOUND, barsFailureErrorResponse)

              whenReady(sut.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
              }
            }
          }
        }
        "AND the response does not match any predetermined CBErrorResponse" - {
          "THEN a ConnectorError with the matched status and description is returned" in {
            forAll(arbitrary[UpdateBankAccountRequest]) { updateBankAccountRequest =>
              val expectedMessage = "Unit Test other failure expected message"
              updateBankAccountFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

              whenReady(sut.updateBankAccount(updateBankAccountRequest).value) { result =>
                result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
              }
            }
          }
        }
      }
    }
  }
}
