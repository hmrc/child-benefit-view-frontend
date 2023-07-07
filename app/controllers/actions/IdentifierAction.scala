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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.IdentifierAction._
import models.common.NationalInsuranceNumber
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{internalId, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.logging.RequestLogger

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
    override val authConnector:  AuthConnector,
    implicit val config:         FrontendAppConfig,
    val parser:                  BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions {

  private implicit val logger = new RequestLogger(this.getClass)

  private val AuthPredicate = (config: FrontendAppConfig) =>
    Individual or Organisation and AuthProviders(GovernmentGateway) and config.confidenceLevel
  private val ChildBenefitRetrievals = nino and internalId

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(AuthPredicate(config))
      .retrieve(ChildBenefitRetrievals) {
        case Some(nino) ~ Some(internalId) =>
          logger.debug("user is authorised: executing action block")
          block(IdentifierRequest(request, NationalInsuranceNumber(nino), true, internalId))
        case _ =>
          logger.warn("user could not be authorised: redirecting")
          Future successful Redirect(
            controllers.routes.UnauthorisedController.onPageLoad
          )
      }
      .recover {
        handleFailure()(config, request)
      }
  }

  private def handleFailure(
  )(implicit config: FrontendAppConfig, request: Request[_]): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      logger.debug("no active session whilst attempting to authorise user: redirecting to login")
      Redirect(
        config.loginUrl,
        Map(
          "origin"   -> Seq(config.appName),
          "continue" -> Seq(resolveCorrectUrl(request))
        )
      )

    case _: InsufficientConfidenceLevel =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      logger.warn("insufficient confidence level whilst attempting to authorise user: redirect to uplift")
      Redirect(
        config.ivUpliftUrl,
        Map(
          "origin"          -> Seq(config.appName),
          "confidenceLevel" -> Seq(config.confidenceLevel.toString),
          "completionURL"   -> Seq(resolveCorrectUrl(request)),
          "failureURL"      -> Seq(toContinueUrl(controllers.routes.UnauthorisedController.onPageLoad)(request))
        )
      )

    case IncorrectNino =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      logger.warn("incorrect none encountered whilst attempting to authorise user")
      Redirect(controllers.routes.UnauthorisedController.onPageLoad)

    case ex: AuthorisationException =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      logger.warn(s"could not authenticate user due to: $ex")
      InternalServerError
  }
}

object IdentifierAction {
  def toContinueUrl(call: Call)(implicit rh: RequestHeader): String = {
    if (call.absoluteURL().contains("://localhost")) {
      call.absoluteURL()
    } else {
      call.url
    }
  }

  def resolveCorrectUrl[A](request: Request[A]): String = {
    val root =
      if (request.host.contains("localhost")) s"http${if (request.secure) "s" else ""}://${request.host}" else ""
    s"$root${request.uri}"
  }
}
