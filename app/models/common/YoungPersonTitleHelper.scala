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

package models.common

import models.ftnae.{FtnaeChildInfo, FtnaeResponse}
import models.requests.DataRequest
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage}
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

case class YoungPersonTitleHelper[A](request: DataRequest[A]) {
  private def childFromConcatenatedChildNamesList(): Option[FtnaeChildInfo] = {
    request.userAnswers.get(FtnaeResponseUserAnswer).map(createMatchList(_)) match {
      case Some(item) => item
      case None       => None
    }
  }
  private def createMatchList(userAnswer: FtnaeResponse) = {
    val childNamesMatchListWithIndex: List[(String, Int)] =
      userAnswer.children.map(toFtnaeChildNameTitleCase(_)).zipWithIndex

    val youngPersonNameAsPerPage = request.userAnswers.get(WhichYoungPersonPage)

    childNamesMatchListWithIndex
      .find(childName => childName._1 == youngPersonNameAsPerPage.getOrElse(""))
      .map(item => userAnswer.children(item._2))
  }

  /**
    * Match on user from UI page, if found, then return the name that matches.
    */
  def firstNameFromConcatenatedChildNames(): Option[String] = {
    childFromConcatenatedChildNamesList().map(_.name.value)
  }
}
