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

package controllers.ftnae

import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, FtnaeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import utils.pages.FtnaeHelper
import views.html.ftnae.PaymentsExtendedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentsExtendedController @Inject() (
    override val messagesApi: MessagesApi,
    identify:                 IdentifierAction,
    getData:                  CBDataRetrievalAction,
    requireData:              FtnaePaymentsExtendedPageDataRequiredActionImpl,
    ftneaService:             FtnaeService,
    val controllerComponents: MessagesControllerComponents,
    featureActions:           FeatureFlagComposedActions,
    view:                     PaymentsExtendedView,
    errorHandler:             ErrorHandler
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport
    with FtnaeHelper {

  def onPageLoad: Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData andThen requireData).async { implicit request =>
      val summaryListRows = buildSummaryRows(request)(messagesWithFixedLangSupport(messagesApi))

      ftneaService
        .submitFtnaeInformation(summaryListRows)
        .fold(
          error => errorHandler.handleError(error),
          details => Ok(view(details._1, details._2.courseDuration))
        )
    }

}
