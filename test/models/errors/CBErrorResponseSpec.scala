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

package models.errors

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class CBErrorResponseSpec extends BaseSpec {
  "CBErrorResponse" - {
    "GIVEN a valid status and description" - {
      "THEN the expected CBErrorResponse is returned" in {
        forAll(randomFailureStatusCode, alphaStr) { (status, description) =>
          val result = CBErrorResponse(status, description)

          result.status mustBe status
          result.description mustBe description
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(randomFailureStatusCode, alphaStr) { (status, description) =>
        val cbErrorResponse = CBErrorResponse(status, description)
        Json.toJson(cbErrorResponse)
      }
    }
  }
}
