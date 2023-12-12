package models.cob

import base.BaseSpec
import models.changeofbank.BankDetails
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class UpdateBankAccountRequestSpec extends BaseSpec {
  "UpdateBankAccountRequest" - {
    "GIVEN a valid update bank information" - {
      "THEN the expected UpdateBankAccountRequest is returned" in {
        forAll(arbitrary[BankDetails]) { updatedBankDetails =>
          val result = UpdateBankAccountRequest(updatedBankDetails)

          result.updatedBankInformation mustBe updatedBankDetails
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[BankDetails]) { updatedBankDetails =>
        val updateBankAccountRequest = UpdateBankAccountRequest(updatedBankDetails)
        Json.toJson(updateBankAccountRequest)
      }
    }
  }
}
