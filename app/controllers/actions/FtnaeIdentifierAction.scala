/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import config.{FeatureAllowlistFilter, FrontendAppConfig}
import play.api.mvc.Results.NotFound
import play.api.{Configuration, Logging}
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{internalId, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.ftnae.FtnaeDisabledView

import scala.concurrent.{ExecutionContext, Future}

trait FtnaeIdentifierAction extends ActionFunction[MessagesRequest, MessagesRequest]

class AuthenticatedFtnaeIdentifierAction @Inject() (
    override val authConnector:  AuthConnector,
    implicit val config:         FrontendAppConfig,
    configuration:               Configuration,
    val allowList:               FeatureAllowlistFilter,
    ftnaeDisabledView:           FtnaeDisabledView
)(implicit val executionContext: ExecutionContext)
    extends FtnaeIdentifierAction
    with AuthorisedFunctions
    with Logging {

  private val AuthPredicate =
    Individual or Organisation and AuthProviders(GovernmentGateway) and ConfidenceLevel.L200
  private val ChildBenefitRetrievals = nino and internalId

  override def invokeBlock[A](
      request: MessagesRequest[A],
      block:   MessagesRequest[A] => Future[Result]
  ): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val isFeatureEnabled =
      configuration.getOptional[Boolean](s"feature-flags.ftnae.enabled").getOrElse(false)

    authorised(AuthPredicate)
      .retrieve(ChildBenefitRetrievals) {
        case Some(_) ~ Some(_) =>
          logger.debug("user is authorised: executing action block")
          enableFtnaeFeature(request, block, isFeatureEnabled)
        case _ =>
          logger.debug("user could not be authorised for ftnae")
          enableFtnaeFeature(request, block, isFeatureEnabled, hideWrapperMenuBar = true)
      }
      .recoverWith {
        case _ =>
          logger.debug("user could not be authorised for ftnae")
          enableFtnaeFeature(request, block, isFeatureEnabled, hideWrapperMenuBar = true)
      }
  }

  private def enableFtnaeFeature[A](
      request:            MessagesRequest[A],
      block:              MessagesRequest[A] => Future[Result],
      isFeatureEnabled:   Boolean,
      hideWrapperMenuBar: Boolean = false
  ) = {
    if (isFeatureEnabled) {
      allowList(_ => block(request))("ftnae", request)
    } else {
      allowList(_ =>
        Future.successful(
          NotFound(
            ftnaeDisabledView(hideWrapperMenuBar)(request, request.messages)
          )
        )
      )("ftnae", request)
    }
  }
}
