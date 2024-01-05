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

class ClaimantSpec extends BaseSpec {
  "Claimant" - {
    "GIVEN a valid name, award value, award start date, award end date, rate value, payment info, address" - {
      forAll(trueFalseCases) { withAdjustment =>
        s"AND an adjustment information ${isOrIsNot(withAdjustment)} provided" - {
          "THEN the expected Claimant is returned" in {
            forAll(
              arbitrary[FullName],
              arbitrary[BigDecimal],
              arbitrary[LocalDate],
              arbitrary[Seq[LastPaymentFinancialInfo]],
              arbitrary[FullAddress],
              arbitrary[AdjustmentInformation]
            ) { (name, decimalValue, date, lastPayments, fullAddress, adjustmentInformation) =>
              val result = Claimant(
                name,
                decimalValue,
                date,
                date,
                decimalValue,
                decimalValue,
                lastPayments,
                fullAddress,
                if (withAdjustment) Some(adjustmentInformation) else None
              )

              result.name mustBe name
              result.awardValue mustBe decimalValue
              result.awardStartDate mustBe date
              result.awardEndDate mustBe date
              result.higherRateValue mustBe decimalValue
              result.standardRateValue mustBe decimalValue
              result.lastPaymentsInfo mustBe lastPayments
              result.fullAddress mustBe fullAddress
              result.adjustmentInformation mustBe (if (withAdjustment) Some(adjustmentInformation) else None)
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[FullName],
        arbitrary[BigDecimal],
        arbitrary[LocalDate],
        arbitrary[Seq[LastPaymentFinancialInfo]],
        arbitrary[FullAddress],
        arbitrary[AdjustmentInformation]
      ) { (name, decimalValue, date, lastPayments, fullAddress, adjustmentInformation) =>
        val claimant = Claimant(
          name,
          decimalValue,
          date,
          date,
          decimalValue,
          decimalValue,
          lastPayments,
          fullAddress,
          Some(adjustmentInformation)
        )
        Json.toJson(claimant)
      }
    }
  }
}
