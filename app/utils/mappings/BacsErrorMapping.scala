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

package utils.mappings
import play.api.data.{FormError, Forms, Mapping}
import play.api.data.format.Formatter

import scala.util.matching.Regex

object BacsErrorMapping {
  def bacsString(): Mapping[String] =
    Forms.of[String](bacsFormatter())

  private val mainError: Regex = """(?<=\[).+?(?=\])""".r

  private def extractMainError(message: String): String = mainError.findFirstIn(message).fold(message)(identity)

  def bacsFormatter(): Formatter[String] =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        data
          .get("bacsError")
          .map { rawValue =>
            if (rawValue.isEmpty) Right(rawValue)
            else {
              val priority = extractMainError(rawValue)
              Left(
                highlightErrorPerPriority(priority)
              )
            }
          }
          .getOrElse(Right(""))
      }
      private def highlightErrorPerPriority(priority: String): List[FormError] = {
        priority match {
          case "priority1" =>
            List(
              FormError("newSortCode", s"newAccountDetails.error.bacs.$priority")
            )
          case "priority2" =>
            List(
              FormError("bacsErrorMiddle", s"newAccountDetails.error.bacs.$priority"),
              FormError("newSortCode", ""),
              FormError("newAccountNumber", "")
            )
          case "priority3" =>
            List(
              FormError("bacsErrorTop", s"newAccountDetails.error.bacs.$priority"),
              FormError("newAccountHoldersName", ""),
              FormError("newSortCode", ""),
              FormError("newAccountNumber", "")
            )
          case "priority4" =>
            List(
              FormError("bacsErrorTop", s"newAccountDetails.error.bacs.$priority"),
              FormError("newAccountHoldersName", s""),
              FormError("newSortCode", ""),
              FormError("newAccountNumber", "")
            )
          case "priority5" =>
            List(
              FormError("bacsErrorTop", s"newAccountDetails.error.bacs.$priority"),
              FormError("newAccountHoldersName", s""),
              FormError("newSortCode", ""),
              FormError("newAccountNumber", "")
            )
          case "priority6" =>
            List(
              FormError("newAccountHoldersName", s"newAccountDetails.error.bacs.$priority")
            )
          case anyOtherError =>
            List(
              FormError("bacsErrorTop", anyOtherError)
            )
        }
      }
      override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
    }
}
