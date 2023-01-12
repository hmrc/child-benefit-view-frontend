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

import connectors.ChangeOfBankConnector
import controllers.{cob, routes}
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.changeofbank.{AccountHolderName, BankAccountNumber, ClaimantBankAccountInformation, ClaimantBankInformation, SortCode}
import models.cob.VerifyBankAccountRequest
import models.errors.ChangeOfBankValidationError
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Request, Result}
import services.ChangeOfBankService._
import uk.gov.hmrc.http.HeaderCarrier
import utils.handlers.ErrorHandler
import utils.helpers.ClaimantBankInformationHelper.formatClaimantBankInformation
import views.html.cob.ChangeAccountView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeOfBankService @Inject() (
    changeOfBankConnector: ChangeOfBankConnector,
    errorHandler:          ErrorHandler
) {

  def getClaimantName(implicit
      ec:           ExecutionContext,
      hc:           HeaderCarrier,
      auditService: AuditService,
      request:      Request[_]
  ): Future[String] =
    changeOfBankConnector.getChangeOfBankClaimantInfo
      .fold(err => errorHandler.handleError(err), res => s"${res.firstForename.value} ${res.surname.value}")
      .map(_.toString)

  def validate(accountHolderName: AccountHolderName, sortCode: SortCode, bankAccountNumber: BankAccountNumber)(implicit
      hc:                         HeaderCarrier,
      ec:                         ExecutionContext
  ): CBEnvelope[Option[String]] =
    changeOfBankConnector
      .verifyClaimantBankAccount(
        VerifyBankAccountRequest(
          accountHolderName,
          sortCode,
          bankAccountNumber
        )
      )
      .biflatMap(c => CBEnvelope(Some(c.message)), _ => CBEnvelope(None))

  def processClaimantInformation(view: ChangeAccountView)(implicit
      ec:                              ExecutionContext,
      hc:                              HeaderCarrier,
      request:                         Request[_],
      messages:                        Messages
  ): CBEnvelope[Result] = {
    for {
      claimantInfo          <- changeOfBankConnector.getChangeOfBankClaimantInfo
      formattedClaimantInfo <- CBEnvelope(formatClaimantBankInformation(claimantInfo))
      childBenefitPage      <- validateToChangeOfBankPage(formattedClaimantInfo, view)
    } yield childBenefitPage
  }

  private def validateToChangeOfBankPage(
      cbi:               ClaimantBankInformation,
      changeAccountView: ChangeAccountView
  )(implicit request:    Request[_], messages: Messages): CBEnvelope[Result] =
    CBEnvelope {

      val accountInfo:  ClaimantBankAccountInformation = cbi.financialDetails.bankAccountInformation
      val claimantName: String                         = s"${cbi.firstForename.value} ${cbi.surname.value}"

      (
        awardEndDateIsInTheFuture(cbi),
        claimantIsHICBCWithAdjustmentEndDateInFuture(cbi),
        rollNumberIsDefined(cbi)
      ) match {
        case (_, true, _) =>
          Right(Redirect(cob.routes.HICBCOptedOutPaymentsController.onPageLoad()))
        case (true, false, false) =>
          Right(Ok(changeAccountView(claimantName, accountInfo)))
        case (true, false, true) =>
          Right(Ok(changeAccountView(claimantName, accountInfo)))
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
