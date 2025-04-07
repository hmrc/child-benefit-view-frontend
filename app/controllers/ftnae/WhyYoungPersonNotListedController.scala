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
import utils.helpers.FtnaeControllerHelper
import views.html.ftnae.WhyYoungPersonNotListedView
import config.FrontendAppConfig

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WhyYoungPersonNotListedController @Inject() (
    override val messagesApi: MessagesApi,
    auth:                     StandardAuthJourney,
    getData:                  CBDataRetrievalAction,
    requireData:              DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    featureActions:           FeatureFlagComposedActions,
    view:                     WhyYoungPersonNotListedView,
    auditService:             AuditService,
    ftnaeService:             FtnaeService,
    appConfig:                FrontendAppConfig
)(implicit
    ec: ExecutionContext
) extends FrontendBaseController
    with I18nSupport
    with FtnaeControllerHelper {

  def onPageLoad: Action[AnyContent] =
    (featureActions.ftnaeAction andThen auth.pertaxAuthActionWithUserDetails andThen getData andThen requireData) {
      implicit request =>
        auditService.auditFtnaeKickOut(
          request.nino.nino,
          "Success",
          ftnaeService.getSelectedChildInfo(request),
          ftnaeService.getSelectedCourseDuration(request),
          ftnaeService.buildAuditData(buildSummaryRows(request))
        )
        Ok(view(appConfig.ftnaeYear))
    }
}
