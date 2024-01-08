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
import play.api.libs.json.JsString

class AccountHolderTypeSpec extends BaseSpec {
  "AccountHolderType" - {
    val accountHolderToJSTestCases = Table(
      ("accountHolderType", "stringRepresentation"),
      (AccountHolderType.Claimant, "CLAIMANT"),
      (AccountHolderType.Joint, "JOINT"),
      (AccountHolderType.SomeoneElse, "SOMEONE_ELSE")
    )
    "GIVEN an AccountHolderType and an expected string value" - {
      forAll(accountHolderToJSTestCases) { (accountHolderType, stringRepresentation) =>
        s"Account holder type: $accountHolderType - expected value: $stringRepresentation" - {
          "THEN the AccountHolderType can be correctly written as a JSValue" in {
            val result = AccountHolderType.writes.writes(accountHolderType).toString

            result mustBe s""""$stringRepresentation""""
          }
          "THEN the string value can be correctly read as an AccountHolderType" in {
            val result = AccountHolderType.reads.reads(JsString(stringRepresentation))

            result.get mustBe accountHolderType
          }
        }
      }
    }
  }
}
