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
