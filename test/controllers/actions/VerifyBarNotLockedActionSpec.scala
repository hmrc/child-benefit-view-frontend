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
import connectors.ChangeOfBankConnector
import models.CBEnvelope
import models.common.NationalInsuranceNumber
import models.errors.{CBError, ClaimantIsLockedOutOfChangeOfBank}
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, RequestHeader, Result}

import scala.concurrent.ExecutionContext
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import services.AuditService
import stubs.AuthStubs._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.TestData.ninoUser
import utils.handlers.ErrorHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyBarNotLockedActionSpec extends BaseAppSpec with MockitoSugar {
  class Harness(cobConnector: ChangeOfBankConnector, errorHandler: ErrorHandler, auditService: AuditService)
      extends VerifyBarNotLockedActionImpl(cobConnector, errorHandler, auditService) {
    def callFilter[A](request: IdentifierRequest[A]): Future[Option[Result]] = this.filter(request)
  }

  implicit val request: IdentifierRequest[AnyContentAsEmpty.type] =
    IdentifierRequest(FakeRequest(GET, ""), NationalInsuranceNumber("123456"), true, "")

  "when bar not locked is verified from connector, the action" - {

    "must move on with the request (open the gate) and return None" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      val cobConnector = mock[ChangeOfBankConnector]
      val errorHandler = mock[ErrorHandler]
      implicit val auditService: AuditService = mock[AuditService]

      when(cobConnector.verifyBARNotLocked()(any[ExecutionContext], any[HeaderCarrier])).thenReturn(CBEnvelope(()))
      when(
        errorHandler.handleError(any[CBError], any[Option[String]])(
          any[AuditService],
          any[RequestHeader],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ).thenReturn(Redirect(
        controllers.routes.ServiceUnavailableController.onPageLoad
      ))

      val action      = new Harness(cobConnector, errorHandler, auditService)
      val fakeRequest = request
      val result: Option[Result] = action.callFilter(fakeRequest).futureValue

      result must equal(None)
    }

  }
  "when bar not locked is NOT verified from connector, the action" - {
    "must NOT move on with the request (close the gate) and redirect to Bar Locked Page" in {

      userLoggedInIsChildBenefitUser(ninoUser)
      val cobConnector = mock[ChangeOfBankConnector]
      val errorHandler = mock[ErrorHandler]
      implicit val auditService: AuditService = mock[AuditService]

      when(cobConnector.verifyBARNotLocked()(any[ExecutionContext], any[HeaderCarrier])).thenReturn(CBEnvelope
        .fromError[ClaimantIsLockedOutOfChangeOfBank, Unit](
          ClaimantIsLockedOutOfChangeOfBank(Status.FORBIDDEN, "BAR LOCKED")
        ))
      when(
        errorHandler.handleError(any[CBError], any[Option[String]])(
          any[AuditService],
          any[RequestHeader],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ).thenReturn(Redirect(
        controllers.cob.routes.BARSLockOutController.onPageLoad()
      ))

      val action      = new Harness(cobConnector, errorHandler, auditService)
      val fakeRequest = request
      val result: Option[Result] = action.callFilter(fakeRequest).futureValue

      result must equal(Some(Redirect(controllers.cob.routes.BARSLockOutController.onPageLoad())))
    }
  }
}
