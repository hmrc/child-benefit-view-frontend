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
