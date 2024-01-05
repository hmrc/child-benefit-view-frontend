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

package models.cob

import base.BaseSpec
import models.changeofbank.{AccountHolderName, BankAccountNumber, SortCode}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class VerifyBankAccountRequestSpec extends BaseSpec {
  "VerifyBankAccountRequest" - {
    "GIVEN a valid account holder name, sort code and bank account number" - {
      "THEN the expected VerifyBankAccountRequest is returned" in {
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
