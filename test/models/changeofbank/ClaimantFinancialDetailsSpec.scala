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
import models.common.AdjustmentReasonCode
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ClaimantFinancialDetailsSpec extends BaseSpec {
  "ClaimantFinancialDetails" - {
    "GIVEN a valid award end date and claimant bank account information" - {
      forAll(trueFalseCases) { withReason =>
        forAll(trueFalseCases) { withEndDate =>
          s"AND an adjustment reason code ${isOrIsNot(withReason)} provided - adjustment end date ${isOrIsNot(withEndDate)} provided" - {
            "THEN the expected ClaimantFinancialDetails is returned" in {
              forAll(
                arbitrary[LocalDate],
                arbitrary[AdjustmentReasonCode],
                arbitrary[LocalDate],
                arbitrary[ClaimantBankAccountInformation]
              ) { (awardEndDate, adjustmentReasonCode, adjustmentEndDate, bankAccountInformation) =>
                val result = ClaimantFinancialDetails(
                  awardEndDate,
                  if (withReason) Some(adjustmentReasonCode) else None,
                  if (withEndDate) Some(adjustmentEndDate) else None,
                  bankAccountInformation
                )

                result.awardEndDate mustBe awardEndDate
                result.adjustmentReasonCode mustBe (if (withReason) Some(adjustmentReasonCode) else None)
                result.adjustmentEndDate mustBe (if (withEndDate) Some(adjustmentEndDate) else None)
                result.bankAccountInformation mustBe bankAccountInformation
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[LocalDate],
        arbitrary[AdjustmentReasonCode],
        arbitrary[LocalDate],
        arbitrary[ClaimantBankAccountInformation]
      ) { (awardEndDate, adjustmentReasonCode, adjustmentEndDate, bankAccountInformation) =>
        val claimantFinancialDetails = ClaimantFinancialDetails(
          awardEndDate,
          Some(adjustmentReasonCode),
          Some(adjustmentEndDate),
          bankAccountInformation
        )
        Json.toJson(claimantFinancialDetails)
      }
    }
  }
}
