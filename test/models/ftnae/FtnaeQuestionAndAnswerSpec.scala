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

class FtnaeQuestionAndAnswerSpec extends BaseSpec {
  "FtnaeQuestionAndAnswer" - {
    "GIVEN a valid question and answer" - {
      "THEN the expected FtnaeQuestionAndAnswer is returned" in {
        forAll(alphaStr, alphaStr) { (question, answer) =>
          val result = FtnaeQuestionAndAnswer(question, answer)

          result.question mustBe question
          result.answer mustBe answer
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(alphaStr, alphaStr) { (question, answer) =>
        val ftnaeQuestionAndAnswer = FtnaeQuestionAndAnswer(question, answer)
        Json.toJson(ftnaeQuestionAndAnswer)
      }
    }
  }
}
