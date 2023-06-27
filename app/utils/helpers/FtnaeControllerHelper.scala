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

import models.requests.BaseDataRequest
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.ftnae._

trait FtnaeControllerHelper {

  def messagesWithFixedLangSupport(messagesApi: MessagesApi): Messages = {
    messagesApi.preferred(Seq(Lang.apply("en"))).messages
  }

  def buildSummaryRows(
      request:         BaseDataRequest[AnyContent]
  )(implicit messages: Messages): Option[List[SummaryListRow]] = {
    val rows = List(
      WhichYoungPersonSummary.row(request.userAnswers),
      WillYoungPersonBeStayingSummary.row(request.userAnswers),
      SchoolOrCollegeSummary.row(request.userAnswers),
      TwelveHoursAWeekSummary.row(request.userAnswers),
      HowManyYearsSummary.row(request.userAnswers),
      WillCourseBeEmployerProvidedSummary.row(request.userAnswers),
      LiveWithYouInUKSummary.row(request.userAnswers)
    ).flatten

    rows match {
      case rows if !rows.isEmpty => Some(rows)
      case _                     => None
    }
  }
}
