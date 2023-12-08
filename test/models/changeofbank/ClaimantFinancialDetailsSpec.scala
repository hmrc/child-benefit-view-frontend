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
