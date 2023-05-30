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
import connectors.FtneaConnector
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.errors.{CBError, FtnaeChildUserAnswersNotRetrieved}
import models.ftnae.HowManyYears.{Oneyear, Twoyears}
import models.ftnae._
import models.requests.{BaseDataRequest, DataRequest, FtnaePaymentsExtendedPageDataRequest}
import models.viewmodels.checkAnswers.WhichYoungPersonSummary
import pages.ftnae.{FtneaResponseUserAnswer, HowManyYearsPage, WhichYoungPersonPage}
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, WrappedRequest}
import repositories.{SessionRepository, FtnaePaymentsExtendedPageSessionRepository}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FtnaeService @Inject() (
    ftneaConnector:                             FtneaConnector,
    sessionRepository:                          SessionRepository,
    ftnaePaymentsExtendedPageSessionRepository: FtnaePaymentsExtendedPageSessionRepository
) {

  def getFtnaeInformation()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[FtneaResponse] = ftneaConnector.getFtneaAccountDetails()

  def submitFtnaeInformation(summaryListRows: Option[List[SummaryListRow]])(implicit
      ec:                                     ExecutionContext,
      hc:                                     HeaderCarrier,
      request:                                BaseDataRequest[AnyContent],
      messages:                               Messages
  ): EitherT[Future, CBError, (String, ChildDetails)] = {

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

    val childDetails: Either[CBError, (String, ChildDetails)] = (maybeMatchedChild, maybeCourseDuration)
      .mapN((child, courseDuration) => {
        val childDetailsWithAuditInfo =
          ChildDetails(courseDuration, child.crn, child.dateOfBirth, buildAuditData(summaryListRows))
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

  private def uploadFtnaeDetailsIfNotFtnaePaymentsExtendedPageSession(
      childDetails: (String, ChildDetails)
  )(implicit
      ec:       ExecutionContext,
      hc:       HeaderCarrier,
      request:  BaseDataRequest[AnyContent],
      messages: Messages
  ) = {
    request match {
      case DataRequest(_, _, _)                          => ftneaConnector.uploadFtnaeDetails(childDetails._2)
      case FtnaePaymentsExtendedPageDataRequest(_, _, _) => CBEnvelope(())
    }
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

  def buildAuditData(
      summaryListRows: Option[List[SummaryListRow]]
  )(implicit messages: Messages): List[FtneaQuestionAndAnswer] = {

    def convertRowElement(content: Content): String = content.asHtml.body

    summaryListRows match {
      case Some(rows) =>
        rows
          .dropWhile(_.key == Key(Text(messages(WhichYoungPersonSummary.keyName)), ""))
          .map(row => FtneaQuestionAndAnswer(convertRowElement(row.key.content), convertRowElement(row.value.content)))
      case _ => List.empty[FtneaQuestionAndAnswer]
    }
  }

}
