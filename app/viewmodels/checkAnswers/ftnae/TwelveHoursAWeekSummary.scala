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

package viewmodels.checkAnswers.ftnae

import controllers.ftnae.routes
import models.{CheckMode, UserAnswers}
import models.viewmodels.govuk.summarylist._
import models.viewmodels.implicits._
import pages.ftnae.TwelveHoursAWeekPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.PageSummary

object TwelveHoursAWeekSummary extends PageSummary {
  val keyName = "twelveHoursAWeek.checkYourAnswersLabel"
  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TwelveHoursAWeekPage).map { answer =>
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = keyName,
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", routes.TwelveHoursAWeekController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("twelveHoursAWeek.change.hidden"))
        )
      )
    }
}
