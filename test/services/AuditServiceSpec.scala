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

import models.audit.{ClaimantEntitlementDetails, ViewPaymentDetailsModel, ViewProofOfEntitlementModel}
import models.entitlement.Child
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.mock
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Writes
import play.api.mvc.{Headers, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.TestData.entitlementResult

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class AuditServiceSpec extends PlaySpec {

  val auditConnector: AuditConnector = mock[AuditConnector]
  val auditor:        AuditService   = new AuditService(auditConnector)

  protected val request: Request[_] =
    FakeRequest().withHeaders(Headers(("referer", "/foo")))
  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()

  val entitlementDetails: Option[ClaimantEntitlementDetails] =
    Some(
      ClaimantEntitlementDetails(
        name = entitlementResult.claimant.name.value,
        address = entitlementResult.claimant.fullAddress.toSingleLineString,
        amount = entitlementResult.claimant.awardValue,
        start = LocalDate.of(2022, 1, 1).toString,
        end = LocalDate.of(2038, 1, 1).toString,
        children =
          for (child <- entitlementResult.children)
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

      auditor.auditProofOfEntitlement("CA123456A", "testStatus", request, Some(entitlementResult))

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
      capturedEvent.status mustBe "testStatus"
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "-"

      capturedEntitlementDetails.name mustBe "John Doe"
      capturedEntitlementDetails.address mustBe "Addressline1 Addressline2 Addressline3 Addressline4 Addressline5 SS1 7JJ"
      LocalDate.parse(capturedEntitlementDetails.start) mustBe LocalDate.now()
      LocalDate.parse(capturedEntitlementDetails.end) mustBe LocalDate.now().plusYears(3)
      capturedEntitlementDetails.children.length mustBe 1

      capturedChild.name.value mustBe "Full Name"
      capturedChild.dateOfBirth.toString mustBe "2012-01-01"
      capturedChild.relationshipStartDate.toString mustBe "2013-01-01"
      capturedChild.relationshipEndDate.get.toString mustBe "2016-01-01"

    }
  }
  "viewPaymentDetails" should {
    "fire event" in {
      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ViewPaymentDetailsModel] =
        ArgumentCaptor.forClass(classOf[ViewPaymentDetailsModel])

      auditor.auditPaymentDetails("CA123456A", "testStatus", request, Some(entitlementResult))

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ViewPaymentDetailsModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ViewPaymentDetailsModel]]()
        )

      val capturedEvent = captor.getValue

      capturedEvent.nino mustBe "CA123456A"
      capturedEvent.status mustBe "testStatus"
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "-"
      capturedEvent.numberOfPaymentsVisibleToUser mustBe 5

    }
  }

}
