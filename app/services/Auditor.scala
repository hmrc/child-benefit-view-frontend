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
import models.entitlement.PaymentFinancialInfo
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class Auditor @Inject()(auditConnector: AuditConnector) {


  def viewProofOfEntitlement(nino: String,
                             status: String,
                             referrer: String,
                             deviceFingerprint: String,
                             entitlementDetails: Option[ClaimantEntitlementDetails]
                            )(implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {

    val payload = ViewProofOfEntitlementModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      entitlementDetails
    )

    auditConnector.sendExplicitAudit(ViewProofOfEntitlementModel.eventType, payload)
  }


  def viewPaymentDetails(nino: String,
                         status: String,
                         referrer: String,
                         deviceFingerprint: String,
                         numOfPayments: Int,
                         payments: Seq[PaymentFinancialInfo]
                        )(implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {

    val payload = ViewPaymentDetailsModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      numOfPayments,
      payments
    )

    auditConnector.sendExplicitAudit(ViewPaymentDetailsModel.eventType, payload)
  }

}
