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

import connectors.ChangeOfBankConnector
import controllers.{cob, routes}
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.changeofbank.ClaimantBankInformation
import models.errors.ChangeOfBankValidationError
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Request, Result}
import services.ChangeOfBankService._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.cob.ChangeAccountView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ChangeOfBankService @Inject() (
    changeOfBankConnector: ChangeOfBankConnector,
    changeAccountView:     ChangeAccountView
) {

  def processClaimantInformation()(implicit
      ec:       ExecutionContext,
      hc:       HeaderCarrier,
      request:  Request[_],
      messages: Messages
  ): CBEnvelope[Result] = {
    for {
      _                <- changeOfBankConnector.verifyClaimantBankAccount
      claimantInfo     <- changeOfBankConnector.getChangeOfBankClaimantInfo
      childBenefitPage <- validateToChangeOfBankPage(claimantInfo)
    } yield childBenefitPage
  }

  private def validateToChangeOfBankPage(
      cbi:            ClaimantBankInformation
  )(implicit request: Request[_], messages: Messages): CBEnvelope[Result] =
    CBEnvelope {
      (
        awardEndDateIsInTheFuture(cbi),
        claimantIsHICBCWithAdjustmentEndDateInFuture(cbi),
        rollNumberIsDefined(cbi)
      ) match {
        case (_, true, _) =>
          Right(Redirect(cob.routes.HICBCOptedOutPaymentsController.onPageLoad()))
        case (true, false, false) =>
          Right(Ok(changeAccountView()))
        case (true, false, true) =>
          Right(Ok(changeAccountView()))
        case (false, _, _) =>
          Right(Redirect(routes.NoAccountFoundController.onPageLoad))
        case _ =>
          Left(ChangeOfBankValidationError(Status.NOT_FOUND))
      }
    }
}

object ChangeOfBankService {
  private val today: LocalDate = LocalDate.now()
  private val HICBCAdjustmentCode = "28"

  val awardEndDateIsInTheFuture: ClaimantBankInformation => Boolean =
    (claimantBankInformation: ClaimantBankInformation) =>
      claimantBankInformation.financialDetails.awardEndDate.isAfter(today)

  val claimantIsHICBCWithAdjustmentEndDateInFuture: ClaimantBankInformation => Boolean =
    (claimantBankInformation: ClaimantBankInformation) => {
      claimantBankInformation.financialDetails.adjustmentReasonCode.exists(
        _.value == HICBCAdjustmentCode
      ) && claimantBankInformation.financialDetails.adjustmentEndDate.exists(_.isAfter(today))
    }

  val rollNumberIsDefined: ClaimantBankInformation => Boolean = (claimantBankInformation: ClaimantBankInformation) =>
    claimantBankInformation.financialDetails.bankAccountInformation.buildingSocietyRollNumber.isDefined
}
