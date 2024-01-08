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
