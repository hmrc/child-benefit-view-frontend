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

import config.FrontendAppConfig
import connectors.ChildBenefitEntitlementConnector
import controllers.PaymentHistoryController.{fireAuditEvent, getStatus}
import controllers.auth.AuthContext
import handlers.ErrorHandler
import models.entitlement.{ChildBenefitEntitlement, LastPaymentFinancialInfo}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import play.api.{Configuration, Environment}
import services.{AuditorService, PaymentHistoryService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.DeviceFingerprint

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentHistoryController @Inject() (
    authConnector:                    AuthConnector,
    paymentHistoryService:            PaymentHistoryService,
    childBenefitEntitlementConnector: ChildBenefitEntitlementConnector,
    errorHandler:                     ErrorHandler
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig,
    auditor:           AuditorService
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] =
    Action.async { implicit request =>
      authorisedAsChildBenefitUser { authContext =>
        paymentHistoryService.retrieveAndValidatePaymentHistory.fold(
          err => errorHandler.handleError(err),
          result => {
            childBenefitEntitlementConnector.getChildBenefitEntitlement.fold(
              err => errorHandler.handleError(err),
              entitlement => {
                fireAuditEvent(request, entitlement, authContext)
              }
            )
            Ok(result)
          }
        )
      }(routes.PaymentHistoryController.view)
    }

}

object PaymentHistoryController {

  private def isTodayOrInPast(date: LocalDate): Boolean = {
    date.isBefore(LocalDate.now) || date.isEqual(LocalDate.now)
  }

  def fireAuditEvent(
      request:        MessagesRequest[AnyContent],
      entitlement:    ChildBenefitEntitlement,
      authContext:    AuthContext[Any]
  )(implicit auditor: AuditorService, hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val nino              = authContext.nino.nino
    val deviceFingerprint = DeviceFingerprint.deviceFingerprintFrom(request)
    val ref = request.headers
      .get("referer") //MDTP header
      .getOrElse(
        request.headers
          .get("Referer") //common browser header
          .getOrElse("Referrer not found")
      )
    val payments = entitlement.claimant.lastPaymentsInfo.map(payment =>
      LastPaymentFinancialInfo(payment.creditDate, payment.creditAmount)
    )

    val status = getStatus(
      entitlement.claimant.awardEndDate,
      payments,
      Some(entitlement.claimant.adjustmentInformation.get.adjustmentReasonCode.value),
      Some(entitlement.claimant.adjustmentInformation.get.adjustmentEndDate)
    )

    auditor.viewPaymentDetails(nino, status, ref, deviceFingerprint, payments.length, payments)

  }

  def getStatus(
      entitlementEndDate:   LocalDate,
      payments:             Seq[LastPaymentFinancialInfo],
      adjustmentReasonCode: Option[String] = None,
      adjustmentEndDate:    Option[LocalDate] = None
  ): String = {

    val paymentDatesWithinTwoYears = payments.map(_.creditDate).filter(_.isAfter(LocalDate.now.minusYears(2)))
    val numOfPayments              = paymentDatesWithinTwoYears.length
    val today                      = LocalDate.now

    if (entitlementEndDate.isAfter(today) && numOfPayments > 0 && adjustmentReasonCode.isEmpty) "Active - Payments"
    else if (numOfPayments > 0 && adjustmentReasonCode.isDefined && isTodayOrInPast(adjustmentEndDate.get))
      "Active - Payments"
    else if (
      entitlementEndDate.isAfter(today) && numOfPayments == 0 && adjustmentReasonCode.isDefined && adjustmentEndDate.get
        .isAfter(today)
    ) "Active - No payments"
    else if (numOfPayments == 0 && adjustmentReasonCode.isDefined && isTodayOrInPast(adjustmentEndDate.get))
      "Active - No payments"
    else if (numOfPayments > 0 && adjustmentReasonCode.isDefined && adjustmentEndDate.get.isAfter(today))
      "HICBC - Payments"
    else if (numOfPayments == 0 && adjustmentReasonCode.isDefined && adjustmentEndDate.get.isAfter(today))
      "HICBC - No payments"
    else if (isTodayOrInPast(entitlementEndDate) && numOfPayments == 0) "Inactive - No payments"
    else if (isTodayOrInPast(entitlementEndDate) && numOfPayments > 0) "Inactive - Payments"
    else "NOT COVERED"
  }

}
