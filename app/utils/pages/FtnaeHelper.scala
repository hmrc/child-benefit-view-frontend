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

package utils.pages

import models.requests.DataRequest
import models.viewmodels.checkAnswers._
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.AnyContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

trait FtnaeHelper {

  def messagesWithFixedLangSupport(messagesApi: MessagesApi): Messages = {
    messagesApi.preferred(Seq(Lang.apply("en"))).messages
  }

  def buildSummaryRows(request: DataRequest[AnyContent])(implicit messages: Messages): Option[List[SummaryListRow]] = {

    for {
      whichYoungPersonRow             <- WhichYoungPersonSummary.row(request.userAnswers)
      willYoungPersonBeStayingRow     <- WillYoungPersonBeStayingSummary.row(request.userAnswers)
      schoolOrCollegeRow              <- SchoolOrCollegeSummary.row(request.userAnswers)
      twelveHoursAWeekRow             <- TwelveHoursAWeekSummary.row(request.userAnswers)
      howManyYearsRow                 <- HowManyYearsSummary.row(request.userAnswers)
      willCourseBeEmployerProvidedRow <- WillCourseBeEmployerProvidedSummary.row(request.userAnswers)
      liveWithYouInUKRow              <- LiveWithYouInUKSummary.row(request.userAnswers)
    } yield List(
      whichYoungPersonRow,
      willYoungPersonBeStayingRow,
      schoolOrCollegeRow,
      twelveHoursAWeekRow,
      howManyYearsRow,
      willCourseBeEmployerProvidedRow,
      liveWithYouInUKRow
    )
  }

}
