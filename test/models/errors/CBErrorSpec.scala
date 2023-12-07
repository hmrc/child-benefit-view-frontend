package models.errors

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}

class CBErrorSpec extends BaseSpec {
  "CBError - case class variations" - {
    def testCase(
      errorType: String, expectedStatus: Int, expectedMessage: String, errorCreator: (Int, String) => CBError
    ) =
      (errorType, errorCreator, expectedStatus, expectedMessage)
    def create(errorCreator: (Int, String) => CBError): (Int, String) => CBError = errorCreator
    val errorMessage = "Unit Test Error Message"

    val errorTypeTestCases = Table(
      ("errorType", "errorCreator", "expectedStatus", "expectedMessage"),
      testCase("ConnectorError", NOT_FOUND, errorMessage, create((status, message) => ConnectorError(status, message))),
      testCase("PaymentHistoryValidationError", NOT_FOUND, errorMessage, create((status, message) => PaymentHistoryValidationError(status, message))),
      testCase("ChangeOfBankValidationError", NOT_FOUND, errorMessage, create((status, message) => ChangeOfBankValidationError(status, message))),
      testCase("PriorityBARSVerificationError", NOT_FOUND, errorMessage, create((status, message) => PriorityBARSVerificationError(status, message))),
      testCase("ClaimantIsLockedOutOfChangeOfBank", NOT_FOUND, errorMessage, create((status, message) => ClaimantIsLockedOutOfChangeOfBank(status, message))),
      testCase("FtnaeNoCHBAccountError", NOT_FOUND, "No ChB Account", create((_, _) => FtnaeNoCHBAccountError)),
      testCase("FtnaeCannotFindYoungPersonError", NOT_FOUND, "Can not find young person", create((_, _) => FtnaeCannotFindYoungPersonError)),
      testCase(
        "FtnaeChildUserAnswersNotRetrieved", INTERNAL_SERVER_ERROR, "Unable to retrieve required user answers",
        create((_, _) => FtnaeChildUserAnswersNotRetrieved)
      )
    )

    "GIVEN a valid status code and message" - {
      forAll(errorTypeTestCases) { (errorType, errorCreator, expectedStatus, expectedMessage) =>
        s"THEN the expected $errorType is returned - status: $expectedStatus | message: $expectedMessage" in {
          val result = errorCreator(expectedStatus, expectedMessage)

          result.statusCode mustBe expectedStatus
          result.message mustBe expectedMessage
        }
      }
    }
  }
}
