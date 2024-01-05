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
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class FtnaeChildInfoSpec extends BaseSpec {
  "FtnaeChildInfo" - {
    "GIVEN a valid child reference number, name, surname, date of birth, claim end date" - {
      forAll(trueFalseCases) { withMidName =>
        s"AND a mid name ${isOrIsNot(withMidName)} provided" - {
          "THEN the expected FtnaeChildInfo is returned" in {
            forAll(
              arbitrary[ChildReferenceNumber],
              arbitrary[FirstForename],
              arbitrary[SecondForename],
              arbitrary[Surname],
              arbitrary[LocalDate],
              arbitrary[LocalDate]
            ) { (referenceNumber, firstName, midName, surname, dateOfBirth, endDate) =>
              val result = FtnaeChildInfo(
                referenceNumber,
                firstName,
                if (withMidName) Some(midName) else None,
                surname,
                dateOfBirth,
                endDate
              )

              result.crn mustBe referenceNumber
              result.name mustBe firstName
              result.midName mustBe (if (withMidName) Some(midName) else None)
              result.lastName mustBe surname
              result.dateOfBirth mustBe dateOfBirth
              result.currentClaimEndDate mustBe endDate
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[ChildReferenceNumber],
        arbitrary[FirstForename],
        arbitrary[SecondForename],
        arbitrary[Surname],
        arbitrary[LocalDate],
        arbitrary[LocalDate]
      ) { (referenceNumber, firstName, midName, surname, dateOfBirth, endDate) =>
        val ftnaeChildInfo = FtnaeChildInfo(referenceNumber, firstName, Some(midName), surname, dateOfBirth, endDate)
        Json.toJson(ftnaeChildInfo)
      }
    }
  }
}
