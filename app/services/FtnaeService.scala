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

package services

import cats.implicits.catsSyntaxTuple2Semigroupal
import connectors.FtneaConnector
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.errors.FtnaeChildUserAnswersNotRetrieved
import models.ftnae.HowManyYears.{Oneyear, Twoyears}
import models.ftnae.{ChildDetails, CourseDuration, FtneaChildInfo, FtneaResponse}
import models.requests.DataRequest
import pages.ftnae.{FtneaResponseUserAnswer, HowManyYearsPage, WhichYoungPersonPage}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FtnaeService @Inject() (
    ftneaConnector: FtneaConnector
) {
  def getFtnaeInformation()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[FtneaResponse] = ftneaConnector.getFtneaAccountDetails()

  def submitFtnaeInformation()(implicit
      ec:      ExecutionContext,
      hc:      HeaderCarrier,
      request: DataRequest[AnyContent]
  ): CBEnvelope[Unit] = {

    val maybeMatchedChild = for {
      ftnaeResp        <- request.userAnswers.get(FtneaResponseUserAnswer)
      selectedChild    <- request.userAnswers.get(WhichYoungPersonPage)
      matchedChildInfo <- selectChildFromList(ftnaeResp.children, selectedChild)
    } yield matchedChildInfo

    val maybeCourseDuration = request.userAnswers.get(HowManyYearsPage) match {
      case Some(Oneyear)  => Some(CourseDuration.OneYear)
      case Some(Twoyears) => Some(CourseDuration.TwoYear)
      case _              => None
    }

    val childDetails = (maybeMatchedChild, maybeCourseDuration)
      .mapN((child, courseDuration) => ChildDetails(courseDuration, child.crn, child.dateOfBirth))
      .toRight(FtnaeChildUserAnswersNotRetrieved)

    CBEnvelope(childDetails).map(details => ftneaConnector.uploadFtnaeDetails(details))
  }

  private def selectChildFromList(children: List[FtneaChildInfo], selectedChild: String): Option[FtneaChildInfo] = {
    val childCrns       = children.map(_.crn.value)
    val noDuplicateCrns = childCrns.distinct.size == childCrns.size
    val onlyOneChildHasTheSelectedName =
      children.map(toFtnaeChildNameTitleCase).count(name => name == selectedChild) == 1

    if (noDuplicateCrns && onlyOneChildHasTheSelectedName) {
      children.find(c => toFtnaeChildNameTitleCase(c) == selectedChild)
    } else None
  }
}
