/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
              s"AND account holder name ${isOrIsNot(withName)} provided - sort code ${isOrIsNot(withSortCode)} provided - bank account number ${isOrIsNot(
                withAccount
              )} provided - building society roll number ${isOrIsNot(withRollNumber)} provided" - {
                "THEN the expected ClaimantBankAccountInformation is returned" in {
                  forAll(
                    arbitrary[AccountHolderName],
                    arbitrary[SortCode],
                    arbitrary[BankAccountNumber],
                    arbitrary[BuildingSocietyRollNumber]
                  ) { (accountHolderName, sortCode, bankAccountNumber, buildingSocietyRollNumber) =>
                    val result = ClaimantBankAccountInformation(
                      if (withName) Some(accountHolderName) else None,
                      if (withSortCode) Some(sortCode) else None,
                      if (withAccount) Some(bankAccountNumber) else None,
                      if (withRollNumber) Some(buildingSocietyRollNumber) else None
                    )

                    result.accountHolderName mustBe (if (withName) Some(accountHolderName) else None)
                    result.sortCode mustBe (if (withSortCode) Some(sortCode) else None)
                    result.bankAccountNumber mustBe (if (withAccount) Some(bankAccountNumber) else None)
                    result.buildingSocietyRollNumber mustBe (if (withRollNumber) Some(buildingSocietyRollNumber)
                                                             else None)
                  }
                }
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[AccountHolderName],
        arbitrary[SortCode],
        arbitrary[BankAccountNumber],
        arbitrary[BuildingSocietyRollNumber]
      ) { (accountHolderName, sortCode, bankAccountNumber, buildingSocietyRollNumber) =>
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
