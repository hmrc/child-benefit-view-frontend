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

package models.ftnae

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class SecondForenameSpec extends BaseSpec {
  "SecondForename" - {
    "GIVEN a valid value" - {
      "THEN the expected SecondForename is returned" in {
        forAll(alphaStr) { value =>
          val result = SecondForename(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(alphaStr) { value =>
        val secondForename = SecondForename(value)
        Json.toJson(secondForename)
      }
    }
  }
}
