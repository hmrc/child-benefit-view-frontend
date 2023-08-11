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

package utils.handlers

import controllers.cob
import models.errors.{CBError, ClaimantIsLockedOutOfChangeOfBank, ConnectorError, FtnaeCannotFindYoungPersonError, FtnaeChildUserAnswersNotRetrieved, FtnaeNoCHBAccountError, PaymentHistoryValidationError}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import play.twirl.api.Html
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import utils.logging.RequestLogger
import views.html.{ErrorTemplate, NotFoundView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ErrorHandler @Inject() (
    val messagesApi: MessagesApi,
    notFoundView:    NotFoundView,
    error:           ErrorTemplate
) extends FrontendErrorHandler
    with I18nSupport {

  private val logger = new RequestLogger(this.getClass)

  override def notFoundTemplate(implicit request: Request[_]): Html =
    notFoundView()

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
      rh:                                       Request[_]
  ): Html =
    error(pageTitle, heading, message)

  def handleError(
      error:               CBError,
      auditOrigin:         Option[String] = None
  )(implicit auditService: AuditService, request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Result = {
    def logMessage(message: String, code: Option[Int]) =
      s"Failed to load: source=${auditOrigin.getOrElse("unknown")} code=${code.getOrElse("N/A")} message=$message"

    error match {
      case ConnectorError(NOT_FOUND, message) if message.contains("NOT_FOUND_CB_ACCOUNT") =>
        logger.info(logMessage("cb account not found", Some(NOT_FOUND)))
        fireAuditEvent(auditOrigin, auditService, request)
        Redirect(controllers.routes.NoAccountFoundController.onPageLoad)
      case e if e.statusCode == INTERNAL_SERVER_ERROR =>
        logger.error(logMessage(e.message, Some(INTERNAL_SERVER_ERROR)))
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case ConnectorError(code, message) =>
        logger.error(logMessage(s"connector error: $message", Some(code)))
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case ClaimantIsLockedOutOfChangeOfBank(code, message) =>
        logger.info(logMessage(s"claimant is locked out due to bars failure: $message", Some(code)))
        Redirect(cob.routes.BARSLockOutController.onPageLoad())
      case PaymentHistoryValidationError(code, message) =>
        logger.error(logMessage(s"payment history validation error: $message", Some(code)))
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case FtnaeNoCHBAccountError =>
        logger.warn(
          logMessage(
            s"Ftnae No Chb Account error: ${FtnaeNoCHBAccountError.message}",
            Some(FtnaeNoCHBAccountError.statusCode)
          )
        )
        Redirect(controllers.routes.NoAccountFoundController.onPageLoad)
      case FtnaeCannotFindYoungPersonError =>
        logger.warn(
          logMessage(
            s"Ftnae can not find young person error: ${FtnaeCannotFindYoungPersonError.message}",
            Some(FtnaeCannotFindYoungPersonError.statusCode)
          )
        )
        Redirect(controllers.ftnae.routes.CannotFindYoungPersonController.onPageLoad())
      case FtnaeChildUserAnswersNotRetrieved =>
        logger.error(
          logMessage(
            s"Ftnae error: ${FtnaeChildUserAnswersNotRetrieved.message}",
            Some(FtnaeChildUserAnswersNotRetrieved.statusCode)
          )
        )
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case _ =>
        logger.error(logMessage("unknown error occurred", None))
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
    }
  }

  private def fireAuditEvent(auditOrigin: Option[String], auditService: AuditService, request: Request[_])(implicit
      hc:                                 HeaderCarrier,
      ec:                                 ExecutionContext
  ): Unit = {
    if (auditOrigin.contains("proofOfEntitlement")) {
      auditService.auditProofOfEntitlement(
        "Unknown",
        "No Accounts Found",
        request,
        None
      )
    } else if (auditOrigin.contains("paymentDetails")) {
      auditService.auditPaymentDetails(
        "nino",
        "No Accounts Found",
        request,
        None
      )
    }
  }
}
