/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package util

import scala.io.Source

object FileUtils {

  def readContent(apiSource: String, fileName: String): String = {
    val filePath = s"./conf/resources/$apiSource/$fileName.json"
    val fileContents = Source.fromFile(filePath).getLines.mkString
    fileContents

  }

}
