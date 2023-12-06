package models.cob

import base.BaseSpec
import play.api.libs.json.Json

class NewAccountDetailsSpec extends BaseSpec {
  "NewAccountDetails" - {
    "GIVEN a valid new account holders name, new sort code and new account number" - {
      "THEN the expected NewAccountDetails is returned" in {
        forAll(generateName, generateSortCode, generateAccountNumber) { (newAccountHoldersName, newSortCode, newAccountNumber) =>
          val result = NewAccountDetails(newAccountHoldersName, newSortCode, newAccountNumber)

          result.newAccountHoldersName mustBe newAccountHoldersName
          result.newSortCode mustBe newSortCode
          result.newAccountNumber mustBe newAccountNumber
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName, generateSortCode, generateAccountNumber) { (newAccountHoldersName, newSortCode, newAccountNumber) =>
        val newAccountDetails = NewAccountDetails(newAccountHoldersName, newSortCode, newAccountNumber)
        Json.toJson(newAccountDetails)
      }
    }
  }
}
