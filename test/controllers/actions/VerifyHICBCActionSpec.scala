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

package controllers.actions

import base.BaseAppSpec
import models.CBEnvelope
import models.changeofbank.{ClaimantBankAccountInformation, ClaimantBankInformation, ClaimantFinancialDetails}
import models.common.{AdjustmentReasonCode, FirstForename, NationalInsuranceNumber, Surname}
import models.errors.CBError
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{AnyContentAsEmpty, Request, Result}

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import services.{AuditService, ChangeOfBankService}
import stubs.AuthStubs._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.TestData.ninoUser
import utils.handlers.ErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyHICBCActionSpec extends BaseAppSpec with MockitoSugar {
  class Harness(cobService: ChangeOfBankService, errorHandler: ErrorHandler, auditService: AuditService)
      extends VerifyHICBCActionImpl(cobService, errorHandler, auditService) {
    def callFilter[A](request: IdentifierRequest[A]): Future[Option[Result]] = this.filter(request)
  }

  def generateCobClaimantInfo(
      adjustmentReasonCode: AdjustmentReasonCode,
      adjustmentEndDate:    LocalDate
  ): ClaimantBankInformation =
    ClaimantBankInformation(
      FirstForename("FirstForename"),
      Surname("Surname"),
      LocalDate.of(1955, 1, 26),
      true,
      ClaimantFinancialDetails(
        LocalDate.now(),
        Some(adjustmentReasonCode),
        Some(adjustmentEndDate),
        ClaimantBankAccountInformation(None, None, None, None)
      )
    )
  implicit val request: IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(FakeRequest(GET, ""), NationalInsuranceNumber("123456"), true, "")
  "when claimant is NOT HICBCWithAdjustmentEndDateInFuture, the action " - {

    "must move on with the request (open the gate) and return None" in {

      userLoggedInIsChildBenefitUser(ninoUser)
      val cobService   = mock[ChangeOfBankService]
      val errorHandler = mock[ErrorHandler]
      implicit val auditService: AuditService = mock[AuditService]

      val someOtherReasonCodeThanHicbc = AdjustmentReasonCode("8")
      when(cobService.retrieveBankClaimantInfo(any[ExecutionContext], any[HeaderCarrier])) thenReturn CBEnvelope(
        generateCobClaimantInfo(someOtherReasonCodeThanHicbc, LocalDate.now().plusDays(2))
      )
      when(
        errorHandler.handleError(any[CBError], any[Option[String]])(
          any[AuditService],
          any[Request[_]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ) thenReturn Redirect(
        controllers.routes.ServiceUnavailableController.onPageLoad
      )

      val action      = new Harness(cobService, errorHandler, auditService)
      val fakeRequest = request
      val result: Option[Result] = action.callFilter(fakeRequest).futureValue

      result must equal(None)
    }
  }

  "when claimant is HICBC but NOT WithAdjustmentEndDateInFuture, the action " - {
    "must move on with the request (open the gate) and return None" in {

      userLoggedInIsChildBenefitUser(ninoUser)
      val cobService   = mock[ChangeOfBankService]
      val errorHandler = mock[ErrorHandler]
      implicit val auditService: AuditService = mock[AuditService]

      val hicbcReasonCode = AdjustmentReasonCode("28")
      when(cobService.retrieveBankClaimantInfo(any[ExecutionContext], any[HeaderCarrier])) thenReturn CBEnvelope(
        generateCobClaimantInfo(hicbcReasonCode, LocalDate.now().minusDays(2))
      )
      when(
        errorHandler.handleError(any[CBError], any[Option[String]])(
          any[AuditService],
          any[Request[_]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ) thenReturn Redirect(
        controllers.routes.ServiceUnavailableController.onPageLoad
      )

      val action      = new Harness(cobService, errorHandler, auditService)
      val fakeRequest = request
      val result: Option[Result] = action.callFilter(fakeRequest).futureValue

      result must equal(None)
    }
  }
  "when claimant is HICBC AND WithAdjustmentEndDateInFuture, the action " - {
    "must NOT move on with the request (close the gate) and redirect to Hicbc opted out page" in {

      userLoggedInIsChildBenefitUser(ninoUser)
      val cobService   = mock[ChangeOfBankService]
      val errorHandler = mock[ErrorHandler]
      implicit val auditService: AuditService = mock[AuditService]
      val hicbcReasonCode = AdjustmentReasonCode("28")
      when(cobService.retrieveBankClaimantInfo(any[ExecutionContext], any[HeaderCarrier])) thenReturn CBEnvelope(
        generateCobClaimantInfo(hicbcReasonCode, LocalDate.now().plusDays(2))
      )
      when(
        errorHandler.handleError(any[CBError], any[Option[String]])(
          any[AuditService],
          any[Request[_]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ) thenReturn Redirect(
        controllers.routes.ServiceUnavailableController.onPageLoad
      )

      val action      = new Harness(cobService, errorHandler, auditService)
      val fakeRequest = request
      val result: Option[Result] = action.callFilter(fakeRequest).futureValue

      result must equal(Some(Redirect(controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad())))
    }
  }
}
