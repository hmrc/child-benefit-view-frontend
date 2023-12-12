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
