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

import models.CBEnvelope
import models.changeofbank.ClaimantBankInformation
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.AuditService
import services.ChangeOfBankService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.handlers.ErrorHandler
import utils.helpers.ClaimantBankInformationHelper.formatClaimantBankInformation

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait VerifyHICBCAction extends ActionFilter[IdentifierRequest]

class VerifyHICBCActionImpl @Inject() (
    changeOfBankService:         ChangeOfBankService,
    errorHandler:                ErrorHandler,
    auditService:                AuditService
)(implicit val executionContext: ExecutionContext)
    extends VerifyHICBCAction
    with Logging {

  private val HICBCAdjustmentCode = "28"

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val r = for {
      claimantInfo <- changeOfBankService.retrieveBankClaimantInfo
      cbi          <- CBEnvelope(formatClaimantBankInformation(claimantInfo))
    } yield cbi

    r.foldF(
      e => Future.successful(Some(errorHandler.handleError(e, None)(auditService, request, hc, executionContext))),
      cbi =>
        if (claimantIsHICBCWithAdjustmentEndDateInFuture(cbi)) {
          Future.successful(Some(Redirect(controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad())))
        } else {
          Future.successful(None)
        }
    )
  }

  private def claimantIsHICBCWithAdjustmentEndDateInFuture: ClaimantBankInformation => Boolean =
    (claimantBankInformation: ClaimantBankInformation) => {
      claimantBankInformation.financialDetails.adjustmentReasonCode.exists(
        _.value == HICBCAdjustmentCode
      ) && claimantBankInformation.financialDetails.adjustmentEndDate.exists(_.isAfter(LocalDate.now()))
    }
}
