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

package utils.helpers

import models.ftnae.FtneaChildInfo

object StringHelper {
  val defaultDelimiters    = List(" ", "-", "'")
  val defaultExceptedWords = List("and")

  def toTitleCase(
      str:           String,
      delimiters:    List[String] = defaultDelimiters,
      exceptedWords: List[String] = defaultExceptedWords
  ): String = {
    val lowercase = str.toLowerCase()
    delimiters.foldRight(lowercase)((delimiter, str) =>
      str.split(delimiter).map(f => if (exceptedWords.contains(f)) f else f.capitalize).mkString(delimiter)
    )
  }

  def isWhitespaceOnly(str: String): Boolean =
    str.forall(_.isWhitespace)

  def toFtnaeChildNameTitleCase(child: FtneaChildInfo): String = {
    val midName = child.midName.map(mn => s"${mn.value} ").getOrElse("")
    toTitleCase(s"${child.name.value} $midName${child.lastName.value}")
  }
}
