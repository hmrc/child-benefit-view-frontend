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
import models.entitlement.ChildBenefitEntitlement
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuditService @Inject() (auditConnector: AuditConnector) {

  def auditProofOfEntitlement(
      nino:              String,
      deviceFingerprint: String,
      referrer:          String,
      entitlement:       ChildBenefitEntitlement
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Unit = {

    val entDetails: Option[ClaimantEntitlementDetails] =
      Some(
        ClaimantEntitlementDetails(
          name = entitlement.claimant.name.value,
          address = entitlement.claimant.fullAddress.toSingleLineString,
          amount = entitlement.claimant.awardValue,
          start = entitlement.claimant.awardStartDate.toString,
          end = entitlement.claimant.awardEndDate.toString,
          children = entitlement.children
        )
      )

    val payload = ViewProofOfEntitlementModel(
      nino,
      "Successful",
      referrer,
      deviceFingerprint,
      entDetails
    )

    auditConnector.sendExplicitAudit(ViewProofOfEntitlementModel.EventType, payload)
  }

  def auditPaymentDetails(
      nino:              String,
      deviceFingerprint: String,
      referrer:          String,
      entitlement:       ChildBenefitEntitlement
  )(implicit hc:         HeaderCarrier, ec: ExecutionContext): Unit = {

    val payments = entitlement.claimant.lastPaymentsInfo

    val status = PaymentHistoryService.getAuditStatus(entitlement)

    val payload = ViewPaymentDetailsModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      payments.length,
      payments
    )

    auditConnector.sendExplicitAudit(ViewPaymentDetailsModel.EventType, payload)
  }

  def isTodayOrInPast(date: LocalDate): Boolean = {
    date.isBefore(LocalDate.now) || date.isEqual(LocalDate.now)
  }

}
