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

package controllers.cob

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, ChangeOfBankService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import views.html.cob.ChangeAccountView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ChangeAccountController @Inject() (
    override val messagesApi: MessagesApi,
    featureActions:           FeatureFlagComposedActions,
    changeOfBankService:      ChangeOfBankService,
    errorHandler:             ErrorHandler,
    getData:                  CobDataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    view:                     ChangeAccountView
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (featureActions.changeBankAction andThen getData).async { implicit request =>
      changeOfBankService
        .processClaimantInformation(view)
        .fold(
          err => errorHandler.handleError(err),
          result => result
        )
    }
}
