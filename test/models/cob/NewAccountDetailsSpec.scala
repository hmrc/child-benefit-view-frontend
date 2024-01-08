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
import play.api.libs.json.Json

class NewAccountDetailsSpec extends BaseSpec {
  "NewAccountDetails" - {
    "GIVEN a valid new account holders name, new sort code and new account number" - {
      "THEN the expected NewAccountDetails is returned" in {
        forAll(generateName, generateSortCode, generateAccountNumber) {
          (newAccountHoldersName, newSortCode, newAccountNumber) =>
            val result = NewAccountDetails(newAccountHoldersName, newSortCode, newAccountNumber)

            result.newAccountHoldersName mustBe newAccountHoldersName
            result.newSortCode mustBe newSortCode
            result.newAccountNumber mustBe newAccountNumber
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName, generateSortCode, generateAccountNumber) {
        (newAccountHoldersName, newSortCode, newAccountNumber) =>
          val newAccountDetails = NewAccountDetails(newAccountHoldersName, newSortCode, newAccountNumber)
          Json.toJson(newAccountDetails)
      }
    }
  }
}
