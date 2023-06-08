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

import models.ftnae.FtneaChildInfo
import models.requests.DataRequest
import pages.ftnae.{FtneaResponseUserAnswer, WhichYoungPersonPage}
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

trait YoungPersonTitleHelper {

  private def childFromConcatenatedChildNames[A]()(implicit request: DataRequest[A]) = {
    request.userAnswers.get(FtneaResponseUserAnswer).map(userAnswer => {
      val childNamesWithIndex: List[(String, Int)] = userAnswer.children.map(toFtnaeChildNameTitleCase(_)).zipWithIndex
      val youngPersonName = request.userAnswers.get(WhichYoungPersonPage)

      childNamesWithIndex.find(childName => childName._1 == youngPersonName.get).map(childFound => {
        userAnswer.children(childFound._2)
      })
    })
  }

  def firstNameFromConcatenatedChildNames[A]()(implicit request: DataRequest[A]): Option[String] = {
    childFromConcatenatedChildNames[A]()(request).map(_.map(_.name).get.value)
  }
}
