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

package models.entitlement

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class LastPaymentFinancialInfoSpec extends BaseSpec {
  "LastPaymentFinancialInfo" - {
    "GIVEN a credit date and credit amount" - {
      "THEN the expected LastPaymentFinancialInfo is returned" in {
        forAll(arbitrary[LocalDate], arbitrary[BigDecimal]) { (creditDate, creditAmount) =>
          val result = LastPaymentFinancialInfo(creditDate, creditAmount)

          result.creditDate mustBe creditDate
          result.creditAmount mustBe creditAmount
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[LocalDate], arbitrary[BigDecimal]) { (creditDate, creditAmount) =>
        val lastPaymentFinancialInfo = LastPaymentFinancialInfo(creditDate, creditAmount)
        Json.toJson(lastPaymentFinancialInfo)
      }
    }
  }
}
