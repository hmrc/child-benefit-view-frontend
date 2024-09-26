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

package services

import cats.data.EitherT
import connectors.ChildBenefitEntitlementConnector
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.entitlement.ChildBenefitEntitlement
import models.errors.{CBError, PaymentHistoryValidationError}
import models.requests.IdentifierRequest
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.twirl.api.HtmlFormat
import services.PaymentHistoryPageVariant._
import services.PaymentHistoryService._
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.ChildBenefitEntitlementHelper.formatChildBenefitEntitlement
import views.html.paymenthistory.{NoPaymentHistory, PaymentHistory}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentHistoryService @Inject() (
    entitlementConnector: ChildBenefitEntitlementConnector,
    paymentHistory:       PaymentHistory,
    noPaymentHistory:     NoPaymentHistory
) {

  def retrieveAndValidatePaymentHistory(implicit
      auditService: AuditService,
      identifier:   IdentifierRequest[_],
      ec:           ExecutionContext,
      hc:           HeaderCarrier,
      messages:     Messages
  ): EitherT[Future, CBError, HtmlFormat.Appendable] = {
    for {
      childBenefitEntitlement <- entitlementConnector.getChildBenefitEntitlement
      formattedEntitlement    <- CBEnvelope(formatChildBenefitEntitlement(childBenefitEntitlement))
      result                  <- validateEntitlementToPage(formattedEntitlement).orElse(validateAdjustmentToPage(formattedEntitlement))
    } yield result
  }

  private def validateAdjustmentToPage(cbe: ChildBenefitEntitlement)(implicit
      auditService:                         AuditService,
      identifier:                           IdentifierRequest[_],
      hc:                                   HeaderCarrier,
      ec:                                   ExecutionContext,
      request:                              RequestHeader,
      messages:                             Messages
  ): CBEnvelope[HtmlFormat.Appendable] =
    CBEnvelope {
      (entitlementEndDateIsInTheFuture(cbe), paymentIssuedInLastTwoYears(cbe), claimantIsHICBC(cbe)) match {
        case (true, true, false) =>
          auditViewPaymentDetails(request, InPaymentWithPaymentsInLastTwoYears, cbe)
          Right(paymentHistory(cbe, InPaymentWithPaymentsInLastTwoYears))
        case (true, true, true) if adjustmentIsTodayOrInThePast(cbe) =>
          auditViewPaymentDetails(request, InPaymentWithPaymentsInLastTwoYears, cbe)
          Right(paymentHistory(cbe, InPaymentWithPaymentsInLastTwoYears))
        case (true, false, false) =>
          auditViewPaymentDetails(request, InPaymentWithoutPaymentsInLastTwoYears, cbe)
          Right(noPaymentHistory(cbe, InPaymentWithoutPaymentsInLastTwoYears))
        case (true, false, true) if adjustmentIsTodayOrInThePast(cbe) =>
          auditViewPaymentDetails(request, InPaymentWithoutPaymentsInLastTwoYears, cbe)
          Right(noPaymentHistory(cbe, InPaymentWithoutPaymentsInLastTwoYears))
        case (true, true, true) if adjustmentIsInTheFuture(cbe) =>
          auditViewPaymentDetails(request, HICBCWithPaymentsInLastTwoYears, cbe)
          Right(paymentHistory(cbe, HICBCWithPaymentsInLastTwoYears))
        case (true, false, true) if adjustmentIsInTheFuture(cbe) =>
          auditViewPaymentDetails(request, HICBCWithoutPaymentsInLastTwoYears, cbe)
          Right(noPaymentHistory(cbe, HICBCWithoutPaymentsInLastTwoYears))
        case _ => Left(PaymentHistoryValidationError(Status.NOT_FOUND, "entitlement validation failed"))
      }
    }

  private def validateEntitlementToPage(
      cbe:            ChildBenefitEntitlement
  )(implicit request: RequestHeader, messages: Messages): CBEnvelope[HtmlFormat.Appendable] =
    CBEnvelope {
      (entitlementEndDateIsTodayOrInThePast(cbe), paymentIssuedInLastTwoYears(cbe), claimantIsHICBC(cbe)) match {
        case (true, true, _) =>
          Right(paymentHistory(cbe, EntitlementEndedButReceivedPaymentsInLastTwoYears))
        case (true, false, true) => Right(noPaymentHistory(cbe, HICBCWithoutPaymentsInLastTwoYearsAndEndDateInPast))
        case (true, false, _) =>
          Right(noPaymentHistory(cbe, EntitlementEndedButNoPaymentsInLastTwoYears))
        case _ => Left(PaymentHistoryValidationError(Status.NOT_FOUND, "entitlement validation failed"))
      }
    }

  private def auditViewPaymentDetails(
      request:     RequestHeader,
      pageVariant: PaymentHistoryPageVariant,
      cbe:         ChildBenefitEntitlement
  )(implicit
      auditService:     AuditService,
      identifier:       IdentifierRequest[_],
      headerCarrier:    HeaderCarrier,
      executionContext: ExecutionContext
  ): Unit = {

    val status = pageVariant match {
      case PaymentHistoryPageVariant.InPaymentWithPaymentsInLastTwoYears                => "Active - Payments"
      case PaymentHistoryPageVariant.InPaymentWithoutPaymentsInLastTwoYears             => "Active - No payments"
      case PaymentHistoryPageVariant.HICBCWithPaymentsInLastTwoYears                    => "HICBC - Payments"
      case PaymentHistoryPageVariant.HICBCWithoutPaymentsInLastTwoYears                 => "HICBC - No payments"
      case PaymentHistoryPageVariant.HICBCWithoutPaymentsInLastTwoYearsAndEndDateInPast => "HICBC - No payments"
      case PaymentHistoryPageVariant.EntitlementEndedButReceivedPaymentsInLastTwoYears  => "Inactive - Payments"
      case PaymentHistoryPageVariant.EntitlementEndedButNoPaymentsInLastTwoYears        => "Inactive - No Payments"
    }
    auditService.auditPaymentDetails(identifier.nino.nino, status, request, Some(cbe))
  }
}

object PaymentHistoryService {
  private val today: LocalDate = LocalDate.now()

  val entitlementEndDateIsInTheFuture: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) => childBenefitEntitlement.claimant.awardEndDate.isAfter(today)

  val entitlementEndDateIsTodayOrInThePast: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) =>
      childBenefitEntitlement.claimant.awardEndDate.isBefore(today.plusDays(1))

  val paymentIssuedInLastTwoYears: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) =>
      childBenefitEntitlement.claimant.lastPaymentsInfo.exists(_.creditDate.isAfter(today.minusYears(2)))

  val claimantIsHICBC: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) =>
      childBenefitEntitlement.claimant.adjustmentInformation.exists(_.adjustmentReasonCode.value == "28")

  val adjustmentIsTodayOrInThePast: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) =>
      childBenefitEntitlement.claimant.adjustmentInformation.exists(adj =>
        adj.adjustmentEndDate.isEqual(today) || adj.adjustmentEndDate.isBefore(today)
      )

  val adjustmentIsInTheFuture: ChildBenefitEntitlement => Boolean =
    (childBenefitEntitlement: ChildBenefitEntitlement) =>
      childBenefitEntitlement.claimant.adjustmentInformation.exists(_.adjustmentEndDate.isAfter(today))
}

sealed trait PaymentHistoryPageVariant

object PaymentHistoryPageVariant {

  case object InPaymentWithPaymentsInLastTwoYears extends PaymentHistoryPageVariant

  case object InPaymentWithoutPaymentsInLastTwoYears extends PaymentHistoryPageVariant

  case object HICBCWithPaymentsInLastTwoYears extends PaymentHistoryPageVariant

  case object HICBCWithoutPaymentsInLastTwoYears extends PaymentHistoryPageVariant

  case object HICBCWithoutPaymentsInLastTwoYearsAndEndDateInPast extends PaymentHistoryPageVariant

  case object EntitlementEndedButReceivedPaymentsInLastTwoYears extends PaymentHistoryPageVariant

  case object EntitlementEndedButNoPaymentsInLastTwoYears extends PaymentHistoryPageVariant

}
