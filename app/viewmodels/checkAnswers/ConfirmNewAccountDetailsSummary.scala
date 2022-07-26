/*
 * Copyright 2022 HM Revenue & Customs
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

package models.viewmodels.checkAnswers

import controllers.cob.routes
import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import models.viewmodels.govuk.summarylist._
import models.viewmodels.implicits._
import pages.cob.ConfirmNewAccountDetailsPage

object ConfirmNewAccountDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ConfirmNewAccountDetailsPage).map { answer =>
      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"confirmNewAccountDetails.$answer"))
        )
      )

      SummaryListRowViewModel(
        key = "confirmNewAccountDetails.checkYourAnswersLabel",
        value = value,
        actions = Seq(
          ActionItemViewModel("site.change", routes.ConfirmNewAccountDetailsController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("confirmNewAccountDetails.change.hidden"))
        )
      )
    }
}
