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

package controllers.auth

import config.FrontendAppConfig
import models.common.NationalInsuranceNumber
import play.api.Mode.Dev
import play.api.{Environment, Logging}
import play.api.mvc._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{credentialRole, internalId, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

final case class AuthContext[A](
    nino:       NationalInsuranceNumber,
    isUser:     Boolean,
    internalId: String,
    request:    Request[A]
)

trait ChildBenefitAuth extends AuthorisedFunctions with AuthRedirects with Logging {
  this: FrontendController =>

  protected type ChildBenefitAction[A] = AuthContext[A] => Future[Result]

  private val AuthPredicate          = AuthProviders(GovernmentGateway)
  private val ChildBenefitRetrievals = nino and credentialRole and internalId

  def authorisedAsChildBenefitUser(body: ChildBenefitAction[Any])(
      loginContinueUrl:                  Call
  )(implicit
      ec:      ExecutionContext,
      hc:      HeaderCarrier,
      request: Request[_],
      config:  FrontendAppConfig,
      env:     Environment
  ): Future[Result] =
    authorisedUser(loginContinueUrl, body)

  def authorisedAsChildBenefitUser(implicit
      ec:               ExecutionContext,
      config:           FrontendAppConfig,
      cc:               ControllerComponents,
      env:              Environment,
      loginContinueUrl: Call
  ): ActionBuilder[AuthContext, AnyContent] =
    new ActionBuilder[AuthContext, AnyContent] {
      override protected def executionContext: ExecutionContext       = ec
      override def parser:                     BodyParser[AnyContent] = cc.parsers.defaultBodyParser

      override def invokeBlock[A](request: Request[A], block: AuthContext[A] => Future[Result]): Future[Result] = {
        implicit val req = request
        implicit val hc  = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        authorisedUser(loginContinueUrl, block)
      }
    }

  private def authorisedUser[A](
      loginContinueUrl: Call,
      block:            ChildBenefitAction[A]
  )(implicit
      ec:      ExecutionContext,
      hc:      HeaderCarrier,
      config:  FrontendAppConfig,
      env:     Environment,
      request: Request[A]
  ) = {
    authorised(AuthPredicate)
      .retrieve(ChildBenefitRetrievals) {
        case Some(nino) ~ Some(User) ~ Some(internalId) =>
          block(AuthContext(NationalInsuranceNumber(nino), isUser = true, internalId, request))
        case _ => Future successful Redirect(controllers.routes.UnauthorisedController.onPageLoad)
      }
      .recover {
        handleFailure(toContinueUrl(loginContinueUrl))
      }
  }

  private def handleFailure(
      loginContinueUrl: String
  )(implicit config:    FrontendAppConfig): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(loginContinueUrl), "origin" -> Seq(config.appName)))

    case IncorrectNino =>
      Redirect(controllers.routes.UnauthorisedController.onPageLoad)

    case ex: AuthorisationException ⇒
      logger.warn(s"could not authenticate user due to: $ex")
      InternalServerError
  }

  private def toContinueUrl(call: Call)(implicit rh: RequestHeader, env: Environment): String = {
    env.mode match {
      case Dev => call.absoluteURL()
      case _   => call.url
    }
  }

}