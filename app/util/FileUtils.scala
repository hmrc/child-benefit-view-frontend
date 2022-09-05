/*
 * Copyright 2022 HM Revenue & Customs
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

package util

import play.api.Logging

import java.io.InputStream
import java.nio.charset.StandardCharsets
import scala.io.{BufferedSource, Source}

object FileUtils extends Logging {
  def readContent(apiSource: String, fileName: String): Either[String, String] = {
    val filePath = s"resources/$apiSource/$fileName.json"
    var stream   = Option.empty[InputStream]
    var source   = Option.empty[BufferedSource]
    try {
      stream = Some(getClass.getClassLoader.getResourceAsStream(filePath))
      source = stream.map(is => Source.fromInputStream(is, StandardCharsets.UTF_8.name()))
      Right(source.map(_.getLines.mkString).getOrElse(""))
    } catch {
      case error: Throwable =>
        logger.error(s"Cannot read file $filePath", error)
        Left(error.getMessage)
    } finally {
      stream.foreach(_.close())
      source.foreach(_.close())
    }
  }
}
