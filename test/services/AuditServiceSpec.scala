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
import models.changeofbank.{AccountHolderName, BankAccountNumber, SortCode}
import models.common.{ChildReferenceNumber, FirstForename, NationalInsuranceNumber, Surname}
import models.entitlement.Child
import models.ftnae.{CourseDuration, FtnaeChildInfo, FtnaeQuestionAndAnswer}
import models.requests.OptionalDataRequest
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
import utils.TestData.{claimantBankInformation, entitlementResult}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class AuditServiceSpec extends PlaySpec {

  val auditConnector: AuditConnector = mock[AuditConnector]
  val auditor:        AuditService   = new AuditService(auditConnector)

  val testNino:   String = "CA123456A"
  val testCRN:    String = "AC654321C"
  val testStatus: String = "testStatus"
  protected val request: Request[_] =
    FakeRequest().withHeaders(Headers(("referer", "/foo")))
  protected val optionalDataRequest: OptionalDataRequest[_] =
    OptionalDataRequest(request, "123", NationalInsuranceNumber(testNino), None)

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
              relationshipEndDate = Some(LocalDate.of(2038, 1, 1)),
              None,
              None,
              None
            )
      )
    )

  "auditProofOfEntitlement" should {
    "fire event" in {
      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ViewProofOfEntitlementModel] =
        ArgumentCaptor.forClass(classOf[ViewProofOfEntitlementModel])

      auditor.auditProofOfEntitlement(testNino, testStatus, request, Some(entitlementResult))

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ViewProofOfEntitlementModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ViewProofOfEntitlementModel]]()
        )

      val capturedEvent = captor.getValue
      val capturedEntitlementDetails: ClaimantEntitlementDetails = capturedEvent.claimantEntitlementDetails.get
      val capturedChild:              Child                      = capturedEntitlementDetails.children.last

      capturedEvent.nino mustBe testNino
      capturedEvent.status mustBe testStatus
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
  "auditChangeOfBankAccountDetails" should {
    "fire event" in {

      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ChangeOfBankAccountDetailsModel] =
        ArgumentCaptor.forClass(classOf[ChangeOfBankAccountDetailsModel])

      auditor.auditChangeOfBankAccountDetails(testNino, testStatus, optionalDataRequest, claimantBankInformation)

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ChangeOfBankAccountDetailsModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ChangeOfBankAccountDetailsModel]]()
        )

      val capturedEvent = captor.getValue

      val capturedPersonalInformation = capturedEvent.personalInformation
      val capturedBankDetails         = capturedEvent.bankDetails
      val capturedViewDetails         = capturedEvent.viewDetails

      capturedEvent.nino mustBe testNino
      capturedEvent.status mustBe testStatus
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "-"

      capturedPersonalInformation.name mustBe "John Doe"
      capturedPersonalInformation.dateOfBirth mustBe LocalDate.of(1955, 1, 26)
      capturedPersonalInformation.nino mustBe "CA123456A"

      capturedBankDetails.firstname mustBe "John"
      capturedBankDetails.surname mustBe "Doe"
      capturedBankDetails.accountHolderName mustBe Some(
        AccountHolderName("Mr J Doe")
      )
      capturedBankDetails.accountNumber mustBe Some(
        BankAccountNumber("12345678")
      )
      capturedBankDetails.sortCode mustBe Some(SortCode("112233"))
      capturedBankDetails.buildingSocietyRollNumber mustBe None

      capturedViewDetails.accountHolderName mustBe "Mr J Doe"
      capturedViewDetails.accountNumber mustBe "****5678"
      capturedViewDetails.sortCode mustBe "**-**-33"
    }
  }
  "auditPaymentDetails" should {
    "fire event" in {
      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ViewPaymentDetailsModel] =
        ArgumentCaptor.forClass(classOf[ViewPaymentDetailsModel])

      auditor.auditPaymentDetails(testNino, testStatus, request, Some(entitlementResult))

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(ViewPaymentDetailsModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[ViewPaymentDetailsModel]]()
        )

      val capturedEvent = captor.getValue

      capturedEvent.nino mustBe testNino
      capturedEvent.status mustBe testStatus
      capturedEvent.referrer mustBe "/foo"
      capturedEvent.deviceFingerprint mustBe "-"
      capturedEvent.numberOfPaymentsVisibleToUser mustBe 5

    }
  }

  "auditFTNAEKickOut" should {
    "fire event" in {
      Mockito.reset(auditConnector)
      val childInfo: FtnaeChildInfo = FtnaeChildInfo(
        ChildReferenceNumber(testCRN),
        FirstForename("John"),
        None,
        Surname("Doe"),
        LocalDate.now(),
        LocalDate.now().plusYears(1)
      )
      val courseDuration: Option[CourseDuration] = Some(CourseDuration.TwoYear)
      val answers: List[FtnaeQuestionAndAnswer] =
        List(
          FtnaeQuestionAndAnswer("Question 1", "Answer A"),
          FtnaeQuestionAndAnswer("Question 2", "Answer B"),
          FtnaeQuestionAndAnswer("Question 3", "Answer C")
        )

      val captor: ArgumentCaptor[FtnaeKickOutModel] =
        ArgumentCaptor.forClass(classOf[FtnaeKickOutModel])

      auditor.auditFtnaeKickOut(testNino, testStatus, Some(childInfo), courseDuration, answers)

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(FtnaeKickOutModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[FtnaeKickOutModel]]()
        )

      val capturedEvent = captor.getValue

      capturedEvent.nino mustBe testNino
      capturedEvent.status mustBe testStatus
      capturedEvent.crn mustBe Some(childInfo.crn.value)
      capturedEvent.courseDuration mustBe Some("TWO_YEAR")
      capturedEvent.dateOfBirth mustBe Some(childInfo.dateOfBirth.toString)
      capturedEvent.name mustBe Some(s"${childInfo.name.value} ${childInfo.lastName.value}")
      capturedEvent.answers.length mustEqual answers.length
      capturedEvent.answers mustEqual answers
    }
  }
}
