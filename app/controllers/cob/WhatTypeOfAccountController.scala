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

import play.api.i18n.MessagesApi
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.navigation.Navigator
import forms.cob.WhatTypeOfAccountFormProvider
import models.{Mode, UserAnswers}
import models.cob.WhatTypeOfAccount
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import controllers.actions._
import views.html.cob.WhatTypeOfAccountView
import pages.cob.WhatTypeOfAccountPage

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import play.api.data.Form

class WhatTypeOfAccountController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    featureActions:           FeatureFlagComposedActions,
    navigator:                Navigator,
    formProvider:             WhatTypeOfAccountFormProvider,
    getData:                  CBDataRetrievalAction,
    verifyBarNotLockedAction: VerifyBarNotLockedAction,
    verifyHICBCAction:        VerifyHICBCAction,
    val controllerComponents: MessagesControllerComponents,
    view:                     WhatTypeOfAccountView
)(implicit ec:                ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen verifyBarNotLockedAction andThen verifyHICBCAction andThen getData) {
      implicit request =>
        val preparedForm = request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(WhatTypeOfAccountPage) match {
          case None => form
          case Some(value) =>
            form.fill(value)
        }

        Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen verifyBarNotLockedAction andThen verifyHICBCAction andThen getData) {
      implicit request =>
        ???
    }

}
