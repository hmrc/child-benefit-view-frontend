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
import models.entitlement.{Child, ChildBenefitEntitlement, LastPaymentFinancialInfo}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.mock
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.TestData.entitlementResult

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class AuditorServiceSpec extends PlaySpec {

  val auditConnector:        AuditConnector   = mock[AuditConnector]
  val auditor:               AuditorService   = new AuditorService(auditConnector)
  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()

  val entitlement: ChildBenefitEntitlement = entitlementResult
  val entitlementDetails: Option[ClaimantEntitlementDetails] =
    Some(
      ClaimantEntitlementDetails(
        name = entitlement.claimant.name.value,
        address = entitlement.claimant.fullAddress.toSingleLineString,
        amount = entitlement.claimant.awardValue,
        start = LocalDate.of(2022, 1, 1).toString,
        end = LocalDate.of(2038, 1, 1).toString,
        children =
          for (child <- entitlement.children)
            yield Child(
              name = child.name,
              dateOfBirth = child.dateOfBirth,
              relationshipStartDate = LocalDate.of(2022, 1, 1),
              relationshipEndDate = Some(LocalDate.of(2038, 1, 1))
            )
      )
    )

  "viewProofOfEntitlement" should {
    "fire event" in {

      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ViewProofOfEntitlementModel] =
        ArgumentCaptor.forClass(classOf[ViewProofOfEntitlementModel])

      auditor.viewProofOfEntitlement(
        nino = "CA123456A",
        status = "Successful",
        referrer = "/foo",
        deviceFingerprint = "fingerprint",
        entitlementDetails = entitlementDetails
      )

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ViewProofOfEntitlementModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ViewProofOfEntitlementModel]]()
        )

      val capturedEvent = captor.getValue
      val capturedEntitlementDetails: ClaimantEntitlementDetails = capturedEvent.claimantEntitlementDetails.get
      val capturedChild:              Child                      = capturedEntitlementDetails.children.last

      capturedEvent.nino mustBe "CA123456A"
      capturedEvent.status mustBe "Successful"
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "fingerprint"

      capturedEntitlementDetails.name mustBe "John Doe"
      capturedEntitlementDetails.address mustBe "AddressLine1 AddressLine2 AddressLine3 AddressLine4 AddressLine5 SS1 7JJ"
      capturedEntitlementDetails.start mustBe "2022-01-01"
      capturedEntitlementDetails.end mustBe "2038-01-01"
      capturedEntitlementDetails.children.length mustBe 1

      capturedChild.name.value mustBe "Full Name"
      capturedChild.dateOfBirth.toString mustBe "2012-01-01"
      capturedChild.relationshipStartDate.toString mustBe "2022-01-01"
      capturedChild.relationshipEndDate.get.toString mustBe "2038-01-01"

    }
  }
  "viewPaymentDetails" should {
    "fire event" in {
      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ViewPaymentDetailsModel] =
        ArgumentCaptor.forClass(classOf[ViewPaymentDetailsModel])

      auditor.viewPaymentDetails(
        nino = "CA123456A",
        status = "Status",
        referrer = "/foo",
        deviceFingerprint = "fingerprint",
        numOfPayments = 5,
        payments = Seq.empty
      )

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ViewPaymentDetailsModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ViewPaymentDetailsModel]]()
        )

      val capturedEvent = captor.getValue

      capturedEvent.nino mustBe "CA123456A"
      capturedEvent.status mustBe "Status"
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "fingerprint"
      capturedEvent.numOfPayments mustBe 5
      capturedEvent.payments mustBe Seq.empty

    }
  }
  "getStatus" should {
    "return relative audit status" when {
      "Entitlement end date in future, No Adjustment code 28 - active account" in {

        val endDate  = LocalDate.now.plusYears(1)
        val payments = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val result   = auditor.getStatus(endDate, payments, None, None)

        result mustBe "Active - Payments"

      }
      "Adjustment code 28 (HICBC) with an end date = today [OR in the past], At least one payment issued in last 2 years - active account" in {

        val endDate           = LocalDate.now
        val payments          = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val reason            = Some("28")
        val adjustmentEndDate = Some(LocalDate.now)

        val result = auditor.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - Payments"

      }

      "Adjustment code 28 (HICBC) with an end date = today OR in the past, No payment issued in last 2 years" in {

        val endDate = LocalDate.now
        val payments = Seq(
          LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400),
          LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(3), 500)
        )
        val reason            = Some("28")
        val adjustmentEndDate = Some(LocalDate.now)

        val result = auditor.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - No payments"
      }
      "when claimant has opt out due to HICBC - one payment at least in the previous two years" in {

        val endDate           = LocalDate.now
        val reason            = Some("28")
        val payments          = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = auditor.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "HICBC - Payments"
      }
      "when claimant has opt out due to HICBC - no payment in the last two years" in {

        val endDate           = LocalDate.now
        val reason            = Some("28")
        val payments          = Seq.empty
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = auditor.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "HICBC - No payments"
      }
      "when claimant has payment suspended - active account" in {
        val endDate           = LocalDate.now.plusYears(2)
        val payments          = Seq.empty
        val reason            = Some("28")
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = auditor.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - No payments"
      }
      "when claimant has no payment - inactive account" in {

        val endDate  = LocalDate.now.minusYears(1)
        val payments = Seq.empty

        val result = auditor.getStatus(endDate, payments, None, None)

        result mustBe "Inactive - No payments"
      }
      "when claimant has at least a single payment within the previous two years - inactive account." in {

        val endDate  = LocalDate.now.minusYears(1)
        val payments = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))

        val result = auditor.getStatus(endDate, payments, None, None)

        result mustBe "Inactive - Payments"
      }
    }
  }

}
