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

package controllers

import controllers.actions.{IdentifierAction, StandardAuthJourney}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.NoAccountFoundView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class NoAccountFoundController @Inject() (
    authConnector: AuthConnector,
    view:          NoAccountFoundView,
    auth:          StandardAuthJourney
)(implicit
    config: Configuration,
    env:    Environment,
    cc:     MessagesControllerComponents
) extends ChildBenefitBaseController(authConnector)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    auth.pertaxAuthActionWithUserDetails async { implicit request =>
      Future successful Ok(view())
    }
}
