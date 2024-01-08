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
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class FtnaeClaimantInfoSpec extends BaseSpec {
  "FtnaeClaimantInfo" - {
    "GIVEN a valid name and surname" - {
      "THEN the expected FtnaeClaimantInfo is returned" in {
        forAll(arbitrary[FirstForename], arbitrary[Surname]) { (forename, surname) =>
          val result = FtnaeClaimantInfo(forename, surname)

          result.name mustBe forename
          result.surname mustBe surname
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[FirstForename], arbitrary[Surname]) { (forename, surname) =>
        val ftnaeClaimantInfo = FtnaeClaimantInfo(forename, surname)
        Json.toJson(ftnaeClaimantInfo)
      }
    }
  }
}
