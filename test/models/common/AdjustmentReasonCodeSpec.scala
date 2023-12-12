package models.common

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class AdjustmentReasonCodeSpec extends BaseSpec {
  "AdjustmentReasonCode" - {
    "GIVEN a valid value" - {
      "THEN the expected AdjustmentReasonCode is returned" in {
        forAll(alphaStr) { value =>
          val result = AdjustmentReasonCode(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(alphaStr) { value =>
        val adjustmentReasonCode = AdjustmentReasonCode(value)
        Json.toJson(adjustmentReasonCode)
      }
    }
  }
}
