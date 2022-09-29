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

package controllers

import controllers.actions.FeatureFlagSupport
import features.{FeatureFlag, FeatureFlagService}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DummyFlagView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DummyFlagController @Inject() (
    val controllerComponents:  MessagesControllerComponents,
    view:                      DummyFlagView,
    override val featureFlags: FeatureFlagService
)(implicit ec:                 ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with FeatureFlagSupport {
  def onPageLoad: Action[AnyContent] = {
    (Action andThen whenEnabled(FeatureFlag.DummyFlag)).async { implicit request =>
      Future.successful(Ok(view()))
    }
  }
}
