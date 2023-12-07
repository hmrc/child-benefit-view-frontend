/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ChildSpec extends BaseSpec {
  "Child" - {
    "GIVEN a valid name, date of birth and relationship start date" - {
      forAll(trueFalseCases) { withEndDate =>
        s"AND a relationship end date ${isOrIsNot(withEndDate)} provided" - {
          "THEN the expected Child is returned" in {
            forAll(arbitrary[FullName], arbitrary[LocalDate], arbitrary[LocalDate], arbitrary[LocalDate]) {
              (name, dateOfBirth, relationshipStartDate, relationshipEndDate) =>
              val result = Child(name, dateOfBirth, relationshipStartDate, if(withEndDate) Some(relationshipEndDate) else None)

              result.name mustBe name
              result.dateOfBirth mustBe dateOfBirth
              result.relationshipStartDate mustBe relationshipStartDate
              result.relationshipEndDate mustBe (if(withEndDate) Some(relationshipEndDate) else None)
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[FullName], arbitrary[LocalDate], arbitrary[LocalDate], arbitrary[LocalDate]) {
        (name, dateOfBirth, relationshipStartDate, relationshipEndDate) =>
        val child = Child(name, dateOfBirth, relationshipStartDate, Some(relationshipEndDate))
        Json.toJson(child)
      }
    }
  }
}
