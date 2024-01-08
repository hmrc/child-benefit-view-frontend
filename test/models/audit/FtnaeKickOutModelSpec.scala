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

package models.audit

import base.BaseSpec
import models.ftnae.FtnaeQuestionAndAnswer
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

import java.time.LocalDate

class FtnaeKickOutModelSpec extends BaseSpec {
  "FtnaeKickOutModel" - {
    "GIVEN a valid nino, status and a list of FTNAE answers " - {
      forAll(trueFalseCases) { withCRN =>
        forAll(trueFalseCases) { withDuration =>
          forAll(trueFalseCases) { withDateOfBirth =>
            forAll(trueFalseCases) { withName =>
              s"AND child reference number ${isOrIsNot(withCRN)} provided - course duration ${isOrIsNot(
                withDuration
              )} provided - date of birth ${isOrIsNot(withDateOfBirth)} provided - name ${isOrIsNot(withName)} provided" - {
                "THEN the expected ClaimantEntitlementDetails is returned" in {
                  forAll(
                    generateNino,
                    alphaStr,
                    arbitrary[List[FtnaeQuestionAndAnswer]],
                    generateReferenceNumber,
                    arbitrary[LocalDate],
                    generateName
                  ) { (nino, arbitraryString, ftnaeQuestionsAndAnswers, referenceNumber, dateOfBirth, name) =>
                    val result = FtnaeKickOutModel(
                      nino,
                      arbitraryString,
                      if (withCRN) Some(referenceNumber) else None,
                      if (withDuration) Some(arbitraryString) else None,
                      if (withDateOfBirth) Some(dateOfBirth.toString) else None,
                      if (withName) Some(name) else None,
                      ftnaeQuestionsAndAnswers
                    )

                    result.nino mustBe nino
                    result.status mustBe arbitraryString
                    result.crn mustBe (if (withCRN) Some(referenceNumber) else None)
                    result.courseDuration mustBe (if (withDuration) Some(arbitraryString) else None)
                    result.dateOfBirth mustBe (if (withDateOfBirth) Some(dateOfBirth.toString) else None)
                    result.name mustBe (if (withName) Some(name) else None)
                    result.answers mustBe ftnaeQuestionsAndAnswers
                  }
                }
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        generateNino,
        alphaStr,
        arbitrary[List[FtnaeQuestionAndAnswer]],
        generateReferenceNumber,
        arbitrary[LocalDate],
        generateName
      ) { (nino, arbitraryString, ftnaeQuestionsAndAnswers, referenceNumber, dateOfBirth, name) =>
        val ftnaeKickOutModel = FtnaeKickOutModel(
          nino,
          arbitraryString,
          Some(referenceNumber),
          Some(arbitraryString),
          Some(dateOfBirth.toString),
          Some(name),
          ftnaeQuestionsAndAnswers
        )
        Json.toJson(ftnaeKickOutModel)
      }
    }
  }
}
