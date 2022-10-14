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

package services

import models.audit.{ClaimantEntitlementDetails, ViewPaymentDetailsModel, ViewProofOfEntitlementModel}
import models.entitlement.LastPaymentFinancialInfo
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuditorService @Inject() (auditConnector: AuditConnector) {

  def viewProofOfEntitlement(
      nino:               String,
      status:             String,
      referrer:           String,
      deviceFingerprint:  String,
      entitlementDetails: Option[ClaimantEntitlementDetails]
  )(implicit hc:          HeaderCarrier, ex: ExecutionContext): Unit = {

    val payload = ViewProofOfEntitlementModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      entitlementDetails
    )

    auditConnector.sendExplicitAudit(ViewProofOfEntitlementModel.EventType, payload)
  }

  def viewPaymentDetails(
      nino:              String,
      status:            String,
      referrer:          String,
      deviceFingerprint: String,
      numOfPayments:     Int,
      payments:          Seq[LastPaymentFinancialInfo]
  )(implicit hc:         HeaderCarrier, ex: ExecutionContext): Unit = {

    val payload = ViewPaymentDetailsModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      numOfPayments,
      payments
    )

    auditConnector.sendExplicitAudit(ViewPaymentDetailsModel.EventType, payload)
  }

  def isTodayOrInPast(date: LocalDate): Boolean = {
    date.isBefore(LocalDate.now) || date.isEqual(LocalDate.now)
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
