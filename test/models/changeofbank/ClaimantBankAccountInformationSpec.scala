package models.changeofbank

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class ClaimantBankAccountInformationSpec extends BaseSpec {
  "ClaimantBankAccountInformation" - {
    "GIVEN [no non-optional values for this type]" - {
      forAll(trueFalseCases) { withName =>
        forAll(trueFalseCases) { withSortCode =>
          forAll(trueFalseCases) { withAccount =>
            forAll(trueFalseCases) { withRollNumber =>
              s"AND account holder name ${isOrIsNot(withName)} provided - sort code ${isOrIsNot(withSortCode)
              } provided - bank account number ${isOrIsNot(withAccount)} provided - building society roll number ${isOrIsNot(withRollNumber)} provided" - {
                "THEN the expected ClaimantBankAccountInformation is returned" in {
                  forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber], arbitrary[BuildingSocietyRollNumber]) {
                    (accountHolderName, sortCode, bankAccountNumber, buildingSocietyRollNumber) =>
                      val result = ClaimantBankAccountInformation(
                        if(withName) Some(accountHolderName) else None,
                        if(withSortCode) Some(sortCode) else None,
                        if(withAccount) Some(bankAccountNumber) else None,
                        if(withRollNumber) Some(buildingSocietyRollNumber) else None
                      )

                      result.accountHolderName mustBe (if(withName) Some(accountHolderName) else None)
                      result.sortCode mustBe (if(withSortCode) Some(sortCode) else None)
                      result.bankAccountNumber mustBe (if(withAccount) Some(bankAccountNumber) else None)
                      result.buildingSocietyRollNumber mustBe (if(withRollNumber) Some(buildingSocietyRollNumber) else None)
                  }
                }
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber], arbitrary[BuildingSocietyRollNumber]) {
        (accountHolderName, sortCode, bankAccountNumber, buildingSocietyRollNumber) =>
        val claimantBankAccountInformation = ClaimantBankAccountInformation(
          Some(accountHolderName),
          Some(sortCode),
          Some(bankAccountNumber),
          Some(buildingSocietyRollNumber)
        )
        Json.toJson(claimantBankAccountInformation)
      }
    }
  }
}
