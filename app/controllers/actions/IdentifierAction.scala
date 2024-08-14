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
import models.common.NationalInsuranceNumber
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, internalId, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.enrolments.HmrcPTChecks

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction
    extends ActionBuilder[IdentifierRequest, AnyContent]
    with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
    override val authConnector:  AuthConnector,
    implicit val config:         FrontendAppConfig,
    val parser:                  BodyParsers.Default,
    hmrcPTChecks:                HmrcPTChecks
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with Logging {

  private val ChildBenefitRetrievals = nino and internalId and allEnrolments

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(ChildBenefitRetrievals) {
        case Some(nino) ~ Some(internalId) ~ allEnrolments =>
          logger.debug("user is authorised: executing action block")
          if (hmrcPTChecks.isHmrcPTEnrolmentPresentAndValid(nino, allEnrolments)) {
            block(IdentifierRequest(request, NationalInsuranceNumber(nino), true, internalId))
          } else {
            Future.successful(
              Redirect(
                s"${config.protectTaxInfoUrl}/protect-tax-info?redirectUrl=${SafeRedirectUrl(request.uri).encodedUrl}"
              )
            )
          }
        case _ =>
          logger.debug("user could not be authorised: redirecting")
          Future successful Redirect(
            controllers.routes.UnauthorisedController.onPageLoad
          )
      }
      .recover {
        handleFailure()
      }
  }

  private def handleFailure(
  ): PartialFunction[Throwable, Result] = {
    case IncorrectNino =>
      logger.warn("incorrect none encountered whilst attempting to authorise user")
      Redirect(controllers.routes.UnauthorisedController.onPageLoad)

    case ex: AuthorisationException =>
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
