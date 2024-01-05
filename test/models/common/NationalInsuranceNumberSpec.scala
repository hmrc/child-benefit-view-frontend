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

package models.common

import base.BaseSpec
import play.api.libs.json.Json

class NationalInsuranceNumberSpec extends BaseSpec {
  "NationalInsuranceNumber" - {
    "GIVEN a valid nino" - {
      "THEN the expected NationalInsuranceNumber is returned" in {
        forAll(generateNino) { nino =>
          val result = NationalInsuranceNumber(nino)

          result.nino mustBe nino
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateNino) { nino =>
        val nationalInsuranceNumber = NationalInsuranceNumber(nino)
        Json.toJson(nationalInsuranceNumber)
      }
    }
  }
}
