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
import pages.ftnae.HowManyYearsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.PageSummary

object HowManyYearsSummary extends PageSummary {
  val keyName = "howManyYears.checkYourAnswersLabel"
  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HowManyYearsPage).map { answer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"howManyYears.$answer"))
        )
      )

      SummaryListRowViewModel(
        key = keyName,
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", routes.HowManyYearsController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("howManyYears.change.hidden"))
        )
      )
    }
}
