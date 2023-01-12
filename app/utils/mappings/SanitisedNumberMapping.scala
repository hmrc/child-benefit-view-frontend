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

object SanitisedNumberMapping {
  def sanitisedNumber(requiredKey: String, formatKey: String): Mapping[String] =
    Forms.of[String](sanitisedNumberFormatter(requiredKey, formatKey))

  val sanitisableCharacters = """[\.\-_\/\s]""".r
  val numericalRegex        = """[0-9]+""".r

  def sanitisedNumberFormatter(requiredKey: String, formatKey: String) =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data
          .get(key)
          .filterNot(_.trim == "")
          .map { rawValue =>
            val sanitizedValue = sanitisableCharacters.replaceAllIn(rawValue, "")
            sanitizedValue match {
              case numericalRegex() => Right(sanitizedValue)
              case _                => Left(Seq(FormError(key, formatKey, rawValue)))
            }
          }
          .getOrElse(Left(Seq(FormError(key, requiredKey))))

      override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
    }
}
