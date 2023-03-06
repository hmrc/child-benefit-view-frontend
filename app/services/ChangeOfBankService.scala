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
import models.changeofbank._
import models.cob.{NewAccountDetails, UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.errors.ChangeOfBankValidationError
import models.requests.OptionalDataRequest
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Request, Result}
import services.ChangeOfBankService._
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.ClaimantBankInformationHelper.formatClaimantBankInformation
import views.html.cob.ChangeAccountView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ChangeOfBankService @Inject() (
    changeOfBankConnector: ChangeOfBankConnector
)(implicit auditService:   AuditService) {

  def retrieveBankClaimantInfo(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ClaimantBankInformation] =
    changeOfBankConnector.getChangeOfBankClaimantInfo

  def validate(accountHolderName: AccountHolderName, sortCode: SortCode, bankAccountNumber: BankAccountNumber)(implicit
      hc:                         HeaderCarrier,
      ec:                         ExecutionContext
  ): CBEnvelope[Unit] = {

    changeOfBankConnector
      .verifyClaimantBankAccount(
        VerifyBankAccountRequest(
          accountHolderName,
          sortCode,
          bankAccountNumber
        )
      )
  }

  def processClaimantInformation(view: ChangeAccountView)(implicit
      ec:                              ExecutionContext,
      hc:                              HeaderCarrier,
      request:                         OptionalDataRequest[_],
      messages:                        Messages
  ): CBEnvelope[Result] = {
    for {
      claimantInfo          <- changeOfBankConnector.getChangeOfBankClaimantInfo
      formattedClaimantInfo <- CBEnvelope(formatClaimantBankInformation(claimantInfo))
      childBenefitPage      <- validateToChangeOfBankPage(formattedClaimantInfo, view)
    } yield {
      auditService.auditChangeOfBankAccountDetails(
        request.nino.nino,
        "Successful",
        request,
        formattedClaimantInfo
      )
      childBenefitPage
    }
  }

  def submitClaimantChangeOfBank(
      currentBankInfo:    ClaimantBankAccountInformation,
      newBankAccountInfo: Option[NewAccountDetails]
  )(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[UpdateBankDetailsResponse] =
    for {
      newInfo <- CBEnvelope(newBankAccountInfo.toRight(ChangeOfBankValidationError(Status.BAD_REQUEST)))
      updateBankDetailsResponse <-
        changeOfBankConnector.updateBankAccount(toUpdateBankAccountRequest(currentBankInfo, newInfo))
    } yield updateBankDetailsResponse

  private def toUpdateBankAccountRequest(
      currentBankInfo:    ClaimantBankAccountInformation,
      newBankAccountInfo: NewAccountDetails
  ) = {
    UpdateBankAccountRequest(
      currentBankInformation = BankDetails(
        AccountHolderType.SomeoneElse,
        currentBankInfo.accountHolderName.getOrElse(AccountHolderName("N/A")),
        currentBankInfo.bankAccountNumber.getOrElse(BankAccountNumber("N/A")),
        currentBankInfo.sortCode.getOrElse(SortCode("N/A"))
      ),
      updatedBankInformation = BankDetails(
        AccountHolderType.SomeoneElse,
        AccountHolderName(newBankAccountInfo.newAccountHoldersName),
        BankAccountNumber(newBankAccountInfo.newAccountNumber),
        SortCode(newBankAccountInfo.newSortCode)
      )
    )
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
