package models.changeofbank

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class BankDetailsSpec extends BaseSpec {
  "BankDetails" - {
    "GIVEN a valid account holder type, account holder name, account number and sort code" - {
      "THEN the expected BackAccountNumber is returned" in {
        forAll(
          arbitrary[AccountHolderType],
          arbitrary[AccountHolderName],
          arbitrary[BankAccountNumber],
          arbitrary[SortCode]
        ) { (accountHolderType, accountHolderName, bankAccountNumber, sortCode) =>
          val result = BankDetails(accountHolderType, accountHolderName, bankAccountNumber, sortCode)

          result.accountHolderType mustBe accountHolderType
          result.accountHolderName mustBe accountHolderName
          result.accountNumber mustBe bankAccountNumber
          result.sortCode mustBe sortCode
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[AccountHolderType],
        arbitrary[AccountHolderName],
        arbitrary[BankAccountNumber],
        arbitrary[SortCode]
      ) { (accountHolderType, accountHolderName, bankAccountNumber, sortCode) =>
        val bankDetails = BankDetails(accountHolderType, accountHolderName, bankAccountNumber, sortCode)
        Json.toJson(bankDetails)
      }
    }
  }
}
