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
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.libs.json.JsString

class CourseDurationSpec extends BaseSpec {
  "CourseDuration" - {
    val courseDurationToJSTestCases = Table(
      ("courseDuration", "stringRepresentation"),
      (CourseDuration.OneYear, "ONE_YEAR"),
      (CourseDuration.TwoYear, "TWO_YEAR")
    )
    "GIVEN an CourseDuration and an expected string value" - {
      forAll(courseDurationToJSTestCases) { (courseDuration, stringRepresentation) =>
        s"Course Duration: $courseDuration - expected value: $stringRepresentation" - {
          "THEN the AccountHolderType can be correctly written as a JSValue" in {
            val result = CourseDuration.writes.writes(courseDuration).toString

            result mustBe s""""$stringRepresentation""""
          }
          "THEN the string value can be correctly read as an AccountHolderType" in {
            val result = CourseDuration.reads.reads(JsString(stringRepresentation))

            result.get mustBe courseDuration
          }
        }
      }
    }
    "toMessage" - {
      "GIVEN a set of CourseDuration paired with it's individual messageKey" - {
        implicit val mockMessages: Messages = mock[Messages]
        def messageForValue(value:       CourseDuration) = s"expected message for ${value.toString}"
        def setupMessages(individualKey: String, courseDuration: CourseDuration) =
          when(mockMessages(s"paymentsExtended.courseDuration.$individualKey"))
            .thenReturn(messageForValue(courseDuration))
        Seq(
          (CourseDuration.OneYear, "oneYear"),
          (CourseDuration.TwoYear, "twoYears")
        ).foreach(set => setupMessages(set._2, set._1))
        "THEN the expected message value is returned" in {
          CourseDuration.values.foreach(courseDuration => {
            val result = courseDuration.toMessage

            result mustBe messageForValue(courseDuration)
          })
        }
      }
    }
  }
}
