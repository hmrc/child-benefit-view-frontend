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
import org.scalacheck.Gen.numStr
import play.api.libs.json.Json

class BuildingSocietyRollNumberSpec extends BaseSpec {
  "BuildingSocietyRollNumber" - {
    "GIVEN a valid value" - {
      "THEN the expected BuildingSocietyRollNumber is returned" in {
        forAll(generateBuildingSocietyNumber) { number =>
          val result = BuildingSocietyRollNumber(number)

          result.value mustBe number
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(numStr) { number =>
        val buildingSocietyRollNumber = BuildingSocietyRollNumber(number)
        Json.toJson(buildingSocietyRollNumber)
      }
    }
  }
}
