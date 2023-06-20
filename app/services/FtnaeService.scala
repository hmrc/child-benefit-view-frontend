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

import cats.data.EitherT
import cats.implicits.catsSyntaxTuple2Semigroupal
import connectors.FtnaeConnector
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.errors.{CBError, FtnaeChildUserAnswersNotRetrieved}
import models.ftnae.HowManyYears.{Oneyear, Twoyears}
import models.ftnae._
import models.requests.{BaseDataRequest, DataRequest, FtnaePaymentsExtendedPageDataRequest}
import models.viewmodels.checkAnswers.WhichYoungPersonSummary
import pages.ftnae.{FtnaeResponseUserAnswer, HowManyYearsPage, WhichYoungPersonPage}
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import repositories.{FtnaePaymentsExtendedPageSessionRepository, SessionRepository}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FtnaeService @Inject() (
                               ftnaeConnector:                             FtnaeConnector,
                               sessionRepository:                          SessionRepository,
                               ftnaePaymentsExtendedPageSessionRepository: FtnaePaymentsExtendedPageSessionRepository
) {

  def getFtnaeInformation()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[FtnaeResponse] = ftnaeConnector.getFtnaeAccountDetails()

  def submitFtnaeInformation(summaryListRows: Option[List[SummaryListRow]])(implicit
      ec:                                     ExecutionContext,
      hc:                                     HeaderCarrier,
      request:                                BaseDataRequest[AnyContent],
      messages:                               Messages
  ): EitherT[Future, CBError, (String, ChildDetails)] = {

    val maybeMatchedChild = getSelectedChildInfo(request)

    val maybeCourseDuration = request.userAnswers.get(HowManyYearsPage) match {
      case Some(Oneyear)  => Some(CourseDuration.OneYear)
      case Some(Twoyears) => Some(CourseDuration.TwoYear)
      case _              => None
    }

    val childDetails: Either[CBError, (String, ChildDetails)] = (maybeMatchedChild, maybeCourseDuration)
      .mapN((child, courseDuration) => {
        val whichYoungPerson = request.userAnswers.get(WhichYoungPersonPage)
        val childDetailsWithAuditInfo =
          ChildDetails(
            courseDuration,
            child.crn,
            child.dateOfBirth,
            whichYoungPerson.getOrElse(""),
            buildAuditData(summaryListRows)
          )
        (toFtnaeChildNameTitleCase(child), childDetailsWithAuditInfo)
      })
      .toRight(FtnaeChildUserAnswersNotRetrieved)

    for {
      childDetails <- CBEnvelope(childDetails)
      _            <- uploadFtnaeDetailsIfNotFtnaePaymentsExtendedPageSession(childDetails)
      _ <- CBEnvelope[Unit](ftnaePaymentsExtendedPageSessionRepository.set(request.userAnswers)).leftFlatMap(_ =>
        CBEnvelope(())
      )
      _ <- CBEnvelope(sessionRepository.clear(request.userAnswers.id))
    } yield childDetails

  }

  def getSelectedChildInfo(request: BaseDataRequest[AnyContent]): Option[FtnaeChildInfo] = {
    for {
      ftnaeResp        <- request.userAnswers.get(FtnaeResponseUserAnswer)
      selectedChild    <- request.userAnswers.get(WhichYoungPersonPage)
      matchedChildInfo <- selectChildFromList(ftnaeResp.children, selectedChild)
    } yield matchedChildInfo
  }

  private def selectChildFromList(children: List[FtnaeChildInfo], selectedChild: String): Option[FtnaeChildInfo] = {
    val childCrns       = children.map(_.crn.value)
    val noDuplicateCrns = childCrns.distinct.size == childCrns.size
    val onlyOneChildHasTheSelectedName =
      children.map(toFtnaeChildNameTitleCase).count(name => name == selectedChild) == 1

    if (noDuplicateCrns && onlyOneChildHasTheSelectedName) {
      children.find(c => toFtnaeChildNameTitleCase(c) == selectedChild)
    } else {
      None
    }
  }

  private def uploadFtnaeDetailsIfNotFtnaePaymentsExtendedPageSession(
      childDetails: (String, ChildDetails)
  )(implicit
      ec:      ExecutionContext,
      hc:      HeaderCarrier,
      request: BaseDataRequest[AnyContent]
  ) = {
    request match {
      case DataRequest(_, _, _, _)                          => ftnaeConnector.uploadFtnaeDetails(childDetails._2)
      case FtnaePaymentsExtendedPageDataRequest(_, _, _, _) => CBEnvelope(())
      case _                                                => CBEnvelope(())
    }
  }

  def buildAuditData(
      summaryListRows: Option[List[SummaryListRow]]
  )(implicit messages: Messages): List[FtnaeQuestionAndAnswer] = {

    def convertRowElement(content: Content): String = content.asHtml.body

    summaryListRows match {
      case Some(rows) =>
        rows
          .dropWhile(_.key == Key(Text(messages(WhichYoungPersonSummary.keyName)), ""))
          .map(row => FtnaeQuestionAndAnswer(convertRowElement(row.key.content), convertRowElement(row.value.content)))
      case _ => List.empty[FtnaeQuestionAndAnswer]
    }
  }

}
