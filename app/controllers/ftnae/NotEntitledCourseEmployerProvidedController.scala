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
import pages.ftnae.WhichYoungPersonPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, FtnaeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.pages.FtnaeHelper
import views.html.ftnae.NotEntitledCourseEmployerProvidedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NotEntitledCourseEmployerProvidedController @Inject() (
  override val messagesApi: MessagesApi,
  identify:                 IdentifierAction,
  getData:                  CBDataRetrievalAction,
  requireData:              DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  featureActions:           FeatureFlagComposedActions,
  view:                     NotEntitledCourseEmployerProvidedView,
  auditService:             AuditService,
  ftnaeService:             FtnaeService
)(implicit
  ec: ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with FtnaeHelper {

  def onPageLoad: Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData andThen requireData) { implicit request =>
      auditService.auditFtnaeKickOut(
        request.nino.nino,
        "Success",
        ftnaeService.getSelectedChildInfo(request),
        request.userAnswers.get(WhichYoungPersonPage),
        ftnaeService.buildAuditData(buildSummaryRows(request))
      )
      Ok(view())
    }
}
