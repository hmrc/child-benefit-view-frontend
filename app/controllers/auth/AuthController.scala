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

package controllers.auth

import config.FrontendAppConfig
import controllers.ChildBenefitBaseController
import controllers.actions.StandardAuthJourney
import controllers.actions.IdentifierAction.toContinueUrl
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AuthConnector
import utils.logging.RequestLogger

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthController @Inject() (
    sessionRepository: SessionRepository,
    authConnector:     AuthConnector,
    auth:          StandardAuthJourney
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig
) extends ChildBenefitBaseController(authConnector)
    with I18nSupport {

  private val logger = new RequestLogger(this.getClass)

  def signOut(): Action[AnyContent] =
    auth.pertaxAuthActionWithUserDetails async { implicit request =>
      sessionRepository
        .clear(request.internalId)
        .map { _ =>
          logger.debug("user signed out: redirecting to survey")
          Redirect(frontendAppConfig.signOutUrl, Map("continue" -> Seq(frontendAppConfig.exitSurveyUrl)))
            .withSession(("feedbackId", request.internalId))
        }
    }

  def signOutNoSurvey(): Action[AnyContent] =
    auth.pertaxAuthActionWithUserDetails async { implicit request =>
      sessionRepository
        .clear(request.internalId)
        .map { _ =>
          logger.debug("user signed out, no survey: continuing")
          Redirect(
            frontendAppConfig.signOutUrl,
            Map("continue" -> Seq(toContinueUrl(routes.SignedOutController.onPageLoad)))
          )
        }
    }
}
