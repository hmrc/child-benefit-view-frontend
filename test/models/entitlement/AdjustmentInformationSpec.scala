package models.entitlement

import base.BaseSpec
import models.common.AdjustmentReasonCode
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class AdjustmentInformationSpec extends BaseSpec {
  "AdjustmentInformation" - {
    "GIVEN a valid adjustment reason code and adjustment end date" - {
      "THEN the expected AdjustmentInformation is returned" in {
        forAll(arbitrary[AdjustmentReasonCode], arbitrary[LocalDate]) { (reasonCode, endDate) =>
          val result = AdjustmentInformation(reasonCode, endDate)

          result.adjustmentReasonCode mustBe reasonCode
          result.adjustmentEndDate mustBe endDate
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[AdjustmentReasonCode], arbitrary[LocalDate]) { (reasonCode, endDate) =>
        val adjustmentInformation = AdjustmentInformation(reasonCode, endDate)
        Json.toJson(adjustmentInformation)
      }
    }
  }
}
