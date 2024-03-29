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

package controllers.cob

import controllers.actions._
import controllers.cob.ConfirmNewAccountDetailsController.buildSummaryRows
import models.cob.{NewAccountDetails, WhatTypeOfAccount}
import models.viewmodels.govuk.summarylist._
import models.viewmodels.implicits._
import models.{CheckMode, Mode, UserAnswers}
import pages.cob.{NewAccountDetailsPage, WhatTypeOfAccountPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cob.ConfirmNewAccountDetailsView

import javax.inject.Inject

class ConfirmNewAccountDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    featureActions:           FeatureFlagComposedActions,
    getData:                  CBDataRetrievalAction,
    requireData:              DataRequiredAction,
    verifyBarNotLockedAction: VerifyBarNotLockedAction,
    verifyHICBCAction:        VerifyHICBCAction,
    val controllerComponents: MessagesControllerComponents,
    view:                     ConfirmNewAccountDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = {
    (featureActions.changeBankAction andThen verifyBarNotLockedAction andThen verifyHICBCAction andThen getData andThen requireData) {
      implicit request =>
        Ok(
          view(
            mode,
            SummaryListViewModel(buildSummaryRows(request.userAnswers))
              .withCssClass("govuk-!-margin-bottom-9")
              .withAttribute("id" -> "account-details-list")
          )
        )
    }
  }
}

object ConfirmNewAccountDetailsController {
  def buildSummaryRows(userAnswers: UserAnswers)(implicit messages: Messages): List[SummaryListRow] = {
    val rows = List(
      getAnswerSummary[WhatTypeOfAccount](
        userAnswers.get(WhatTypeOfAccountPage),
        "accountType",
        a => a.message(),
        routes.WhatTypeOfAccountController.onPageLoad(CheckMode)
      ),
      getAnswerSummary[NewAccountDetails](
        userAnswers.get(NewAccountDetailsPage),
        "accountHoldersName",
        a => a.newAccountHoldersName,
        routes.NewAccountDetailsController.onPageLoad(CheckMode)
      ),
      getAnswerSummary[NewAccountDetails](
        userAnswers.get(NewAccountDetailsPage),
        "sortCode",
        a => a.newSortCode,
        routes.NewAccountDetailsController.onPageLoad(CheckMode)
      ),
      getAnswerSummary[NewAccountDetails](
        userAnswers.get(NewAccountDetailsPage),
        "accountNumber",
        a => a.newAccountNumber,
        routes.NewAccountDetailsController.onPageLoad(CheckMode)
      )
    ).flatten

    rows match {
      case rows if !rows.isEmpty => rows
      case _                     => List.empty
    }
  }

  private def getAnswerSummary[A](
      answers:    Option[A],
      answerKey:  String,
      getValue:   A => String,
      changeCall: Call
  )(implicit
      messages: Messages
  ): Option[SummaryListRow] = {
    answers.map { answer =>
      SummaryListRowViewModel(
        key = s"confirmNewAccountDetails.summary.$answerKey.label",
        value = ValueViewModel(HtmlContent(HtmlFormat.escape(getValue(answer)))),
        actions = Seq(
          ActionItemViewModel("site.change", changeCall.url)
            .withVisuallyHiddenText(messages(s"confirmNewAccountDetails.summary.$answerKey.change.hidden"))
        )
      )
    }
  }
}
