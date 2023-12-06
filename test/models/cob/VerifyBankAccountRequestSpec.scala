package models.cob

import base.BaseSpec
import models.changeofbank.{AccountHolderName, BankAccountNumber, SortCode}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class VerifyBankAccountRequestSpec extends BaseSpec {
  "VerifyBankAccountRequest" - {
    "GIVEN a valid account holder name, sort code and bank account number" - {
      "THEN the expected UpdateBankDetailsResponse is returned" in {
        forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber]) {
          (accountHolderName, sortCode, bankAccountNumber) =>
          val result = VerifyBankAccountRequest(accountHolderName, sortCode, bankAccountNumber)

          result.accountHolderName mustBe accountHolderName
          result.sortCode mustBe sortCode
          result.bankAccount mustBe bankAccountNumber
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber]) {
        (accountHolderName, sortCode, bankAccountNumber) =>
        val verifyBankAccountRequest = VerifyBankAccountRequest(accountHolderName, sortCode, bankAccountNumber)
        Json.toJson(verifyBankAccountRequest)
      }
    }
  }
}
