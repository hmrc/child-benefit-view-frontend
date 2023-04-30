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

package models.ftnae

import play.api.i18n.Messages
import play.api.libs.json.{JsPath, Reads, Writes}

sealed trait CourseDuration

object CourseDuration {

  object OneYear extends CourseDuration
  object TwoYear extends CourseDuration

  implicit val reads: Reads[CourseDuration] = JsPath
    .read[String]
    .map {
      case "ONE_YEAR" => OneYear
      case "TWO_YEAR" => TwoYear
    }

  implicit val writes: Writes[CourseDuration] = implicitly[Writes[String]].contramap[CourseDuration] {
    case OneYear => "ONE_YEAR"
    case TwoYear => "TWO_YEAR"
  }

  implicit class ImplicitCourseDuration(courseDuration: CourseDuration) {
    implicit def toMessage(implicit messages: Messages): String =
      courseDuration match {
        case OneYear => messages("paymentsExtended.courseDuration.oneYear")
        case TwoYear => messages("paymentsExtended.courseDuration.twoYears")
      }
  }

}
