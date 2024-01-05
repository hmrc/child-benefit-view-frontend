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
import models.common.ChildReferenceNumber
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ChildDetailsSpec extends BaseSpec {
  "ChildDetails" - {
    "GIVEN a valid courseDuration, child reference number, date of birth, which young person and a list of ftnae questions and answers" - {
      "THEN the expected ChildDetails is returned" in {
        forAll(
          arbitrary[CourseDuration],
          arbitrary[ChildReferenceNumber],
          arbitrary[LocalDate],
          generateName,
          arbitrary[List[FtnaeQuestionAndAnswer]]
        ) { (courseDuration, referenceNumber, dateOfBirth, whichYoungPerson, questionsAndAnswers) =>
          val result = ChildDetails(courseDuration, referenceNumber, dateOfBirth, whichYoungPerson, questionsAndAnswers)

          result.courseDuration mustBe courseDuration
          result.crn mustBe referenceNumber
          result.dateOfBirth mustBe dateOfBirth
          result.whichYoungPerson mustBe whichYoungPerson
          result.ftnaeQuestionsAndAnswers mustBe questionsAndAnswers
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[CourseDuration],
        arbitrary[ChildReferenceNumber],
        arbitrary[LocalDate],
        generateName,
        arbitrary[List[FtnaeQuestionAndAnswer]]
      ) { (courseDuration, referenceNumber, dateOfBirth, whichYoungPerson, questionsAndAnswers) =>
        val childDetails =
          ChildDetails(courseDuration, referenceNumber, dateOfBirth, whichYoungPerson, questionsAndAnswers)
        Json.toJson(childDetails)
      }
    }
  }
}
