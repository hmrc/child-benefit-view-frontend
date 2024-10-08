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

import controllers.actions.StandardAuthJourney
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders._
import utils.logging.RequestLogger
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

import javax.inject.Inject
import scala.concurrent.Future

class JourneyRecoveryController @Inject() (
    continueView:   JourneyRecoveryContinueView,
    startAgainView: JourneyRecoveryStartAgainView,
    authConnector:  AuthConnector,
    auth:           StandardAuthJourney
)(implicit
    config: Configuration,
    env:    Environment,
    cc:     MessagesControllerComponents
) extends ChildBenefitBaseController(authConnector)
    with I18nSupport {

  private val logger = new RequestLogger(this.getClass)

  def onPageLoad(continueUrl: Option[RedirectUrl] = None): Action[AnyContent] =
    auth.pertaxAuthActionWithUserDetails async { implicit request =>
      val safeUrl: Option[String] = continueUrl.flatMap { unsafeUrl =>
        unsafeUrl.getEither(OnlyRelative) match {
          case Right(safeUrl) =>
            Some(safeUrl.url)
          case Left(message) =>
            logger.info(message)
            None
        }
      }

      safeUrl
        .map(url => Future successful Ok(continueView(url)))
        .getOrElse(Future successful Ok(startAgainView()))
    }
}
