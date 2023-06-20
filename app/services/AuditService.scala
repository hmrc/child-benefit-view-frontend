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

import models.audit._
import models.changeofbank.ClaimantBankInformation
import models.entitlement.ChildBenefitEntitlement
import models.ftnae.{FtnaeChildInfo, FtnaeQuestionAndAnswer}
import models.requests.OptionalDataRequest
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.DeviceFingerprint
import views.ViewUtils.{formatSensitiveAccNumber, formatSensitiveSort}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuditService @Inject() (auditConnector: AuditConnector) {

  def auditProofOfEntitlement(
      nino:        String,
      status:      String,
      request:     Request[_],
      entitlement: Option[ChildBenefitEntitlement] = None
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Unit = {

    val deviceFingerprint = DeviceFingerprint.deviceFingerprintFrom(request)

    val referrer = request.headers
      .get("referer") //MDTP header
      .getOrElse(
        request.headers
          .get("Referer") //common browser header
          .getOrElse("Referrer not found")
      )

    val entDetails: Option[ClaimantEntitlementDetails] = {
      if (entitlement.isEmpty) None
      else {
        val cbe: ChildBenefitEntitlement = entitlement.get
        Some(
          ClaimantEntitlementDetails(
            name = cbe.claimant.name.value,
            address = cbe.claimant.fullAddress.toSingleLineString,
            amount = cbe.claimant.awardValue,
            start = cbe.claimant.awardStartDate.toString,
            end = cbe.claimant.awardEndDate.toString,
            children = cbe.children
          )
        )
      }
    }

    val payload = ViewProofOfEntitlementModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      entDetails
    )

    auditConnector.sendExplicitAudit(ViewProofOfEntitlementModel.EventType, payload)
  }

  def auditChangeOfBankAccountDetails(
      nino:                  String,
      status:                String,
      request:               OptionalDataRequest[_],
      formattedClaimantInfo: ClaimantBankInformation
  )(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
  ): Unit = {

    val referrer = request.headers
      .get("referer") //MDTP header
      .getOrElse(
        request.headers
          .get("Referer") //common browser header
          .getOrElse("Referrer not found")
      )

    val deviceFingerprint = DeviceFingerprint.deviceFingerprintFrom(request)

    val claimantName: String = s"${formattedClaimantInfo.firstForename.value} ${formattedClaimantInfo.surname.value}"

    val personalInformation = PersonalInformation(
      claimantName,
      formattedClaimantInfo.dateOfBirth,
      nino
    )

    val bankDetails = BankDetails(
      formattedClaimantInfo.firstForename.value,
      formattedClaimantInfo.surname.value,
      formattedClaimantInfo.financialDetails.bankAccountInformation.accountHolderName,
      formattedClaimantInfo.financialDetails.bankAccountInformation.bankAccountNumber,
      formattedClaimantInfo.financialDetails.bankAccountInformation.sortCode,
      formattedClaimantInfo.financialDetails.bankAccountInformation.buildingSocietyRollNumber
    )

    val viewDetails = ViewDetails(
      formattedClaimantInfo.financialDetails.bankAccountInformation.accountHolderName.fold(claimantName)(_.value),
      formatSensitiveAccNumber(
        formattedClaimantInfo.financialDetails.bankAccountInformation.bankAccountNumber
          .fold("Account not found")(_.number)
      ),
      formatSensitiveSort(
        formattedClaimantInfo.financialDetails.bankAccountInformation.sortCode.fold("Sort not found")(_.value)
      )
    )

    val payload = ChangeOfBankAccountDetailsModel(
      nino,
      status,
      referrer,
      deviceFingerprint,
      personalInformation,
      bankDetails,
      viewDetails
    )

    auditConnector.sendExplicitAudit(ChangeOfBankAccountDetailsModel.EventType, payload)
  }

  def auditPaymentDetails(
      nino:        String,
      status:      String,
      request:     Request[_],
      entitlement: Option[ChildBenefitEntitlement]
  )(implicit hc:   HeaderCarrier, ec: ExecutionContext): Unit = {

    val deviceFingerprint = DeviceFingerprint.deviceFingerprintFrom(request)

    val referrer = request.headers
      .get("referer") //MDTP header
      .getOrElse(
        request.headers
          .get("Referer") //common browser header
          .getOrElse("Referrer not found")
      )

    val payments = if (entitlement.isDefined) entitlement.get.claimant.lastPaymentsInfo else Seq.empty

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

  def auditFtnaeKickOut(
      nino:           String,
      status:         String,
      child:          Option[FtnaeChildInfo],
      courseDuration: Option[String],
      answers:        List[FtnaeQuestionAndAnswer]
  )(implicit hc:      HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = FtnaeKickOutModel(
      nino,
      status,
      child.map(_.crn.value),
      courseDuration,
      child.map(_.dateOfBirth.toString),
      child.map(_.name.value),
      answers
    )

    auditConnector.sendExplicitAudit(FtnaeKickOutModel.EventType, payload)
  }
}
