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

package handlers

import models.errors.{CBError, ConnectorError, PaymentHistoryValidationError}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{AnyContent, MessagesRequest, Request, Result}
import play.twirl.api.Html
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.{ErrorTemplate, NotFoundView}
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

@Singleton
class ErrorHandler @Inject() (
    val messagesApi: MessagesApi,
    notFoundView:    NotFoundView,
    error:           ErrorTemplate
)() extends FrontendErrorHandler
    with I18nSupport {

  override def notFoundTemplate(implicit request: Request[_]): Html =
    notFoundView()

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
      rh:                                       Request[_]
  ): Html =
    error(pageTitle, heading, message)

  def handleError(
      error:          CBError,
      auditOrigin:    Option[String] = None
  )(implicit auditor: AuditService, request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Result = {
    val internalServerErrorDefaultPage = InternalServerError(
      standardErrorTemplate(
        "global.error.InternalServerError500.title",
        "global.error.InternalServerError500.heading",
        error.message
      )
    )

    error match {
      case ConnectorError(NOT_FOUND, message) if message.contains("NOT_FOUND_CB_ACCOUNT") =>
        fireAuditEvent(auditOrigin, auditor, request)
        Redirect(controllers.routes.NoAccountFoundController.onPageLoad)
      case e if e.statusCode == INTERNAL_SERVER_ERROR => internalServerErrorDefaultPage
      case ConnectorError(_, _) =>
        Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case PaymentHistoryValidationError(_, _) => Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)
      case _ =>
        internalServerErrorDefaultPage
    }
  }

  private def fireAuditEvent(auditOrigin: Option[String], auditor: AuditService, request: Request[_])(implicit
      hc:                                 HeaderCarrier,
      ec:                                 ExecutionContext
  ): Unit = {
    if (auditOrigin.contains("proofOfEntitlement"))
      auditor.auditProofOfEntitlement(
        "Unknown",
        "No Accounts Found",
        request.asInstanceOf[MessagesRequest[AnyContent]],
        None
      )
    else if (auditOrigin.contains("paymentDetails"))
      auditor.auditPaymentDetails(
        "nino",
        "No Accounts Found",
        request,
        None
      )
  }
}
