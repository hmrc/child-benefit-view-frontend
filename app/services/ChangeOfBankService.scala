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

import com.google.inject.Inject
import connectors.ChangeOfBankConnector
import controllers.routes
import models.CBEnvelope
import models.CBEnvelope.CBEnvelope
import models.changeofbank.*
import models.cob.{NewAccountDetails, UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.errors.ChangeOfBankValidationError
import models.requests.{BaseDataRequest, OptionalDataRequest}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{AnyContent, RequestHeader, Result}
import repositories.SessionRepository
import services.ChangeOfBankService.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.ClaimantBankInformationHelper.{formatBankAccountInformation, formatClaimantBankInformation}
import views.html.cob.ChangeAccountView

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class ChangeOfBankService @Inject() (
    changeOfBankConnector: ChangeOfBankConnector,
    sessionRepository:     SessionRepository
)(implicit auditService:   AuditService) {

  def retrieveBankClaimantInfo(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ClaimantBankInformation] =
    for {
      claimantInfo          <- changeOfBankConnector.getChangeOfBankClaimantInfo
      formattedClaimantInfo <- CBEnvelope(formatClaimantBankInformation(claimantInfo))
    } yield formattedClaimantInfo

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
      request:                         OptionalDataRequest[?],
      messages:                        Messages
  ): CBEnvelope[Result] = {
    for {
      claimantInfo     <- retrieveBankClaimantInfo
      viewClaimantInfo <- CBEnvelope(formatBankAccountInformation(claimantInfo))
      childBenefitPage <- validateToChangeOfBankPage(viewClaimantInfo, view)
    } yield {
      auditService.auditChangeOfBankAccountDetails(
        request.nino.nino,
        "Successful",
        request,
        claimantInfo
      )
      childBenefitPage
    }
  }

  def submitClaimantChangeOfBank(
      newBankAccountInfo: Option[NewAccountDetails],
      request:            BaseDataRequest[AnyContent]
  )(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[UpdateBankDetailsResponse] =
    for {
      newInfo                   <- CBEnvelope(newBankAccountInfo.toRight(ChangeOfBankValidationError(Status.BAD_REQUEST)))
      updateBankDetailsResponse <- changeOfBankConnector.updateBankAccount(toUpdateBankAccountRequest(newInfo))
      _                         <- CBEnvelope(sessionRepository.clear(request.userAnswers.id))
    } yield updateBankDetailsResponse

  def dropChangeOfBankCache()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[Unit] = {
    changeOfBankConnector.dropChangeOfBankCache()
  }

  private def toUpdateBankAccountRequest(
      newBankAccountInfo: NewAccountDetails
  ) = {
    UpdateBankAccountRequest(
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
  )(implicit request:    RequestHeader, messages: Messages): CBEnvelope[Result] =
    CBEnvelope {

      val accountInfo:  ClaimantBankAccountInformation = cbi.financialDetails.bankAccountInformation
      val claimantName: String                         = s"${cbi.firstForename.value} ${cbi.surname.value}"

      (
        awardEndDateIsInTheFuture(cbi),
        rollNumberIsDefined(cbi)
      ) match {
        case (true, false) =>
          Right(Ok(changeAccountView(claimantName, accountInfo)))
        case (true, true) =>
          Right(Ok(changeAccountView(claimantName, accountInfo)))
        case (false, _) =>
          Right(Redirect(routes.NoAccountFoundController.onPageLoad))
        case _ =>
          Left(ChangeOfBankValidationError(Status.NOT_FOUND))
      }
    }
}

object ChangeOfBankService {
  val today: LocalDate = LocalDate.now()
  val HICBCAdjustmentCode = "28"

  val awardEndDateIsInTheFuture: ClaimantBankInformation => Boolean =
    (claimantBankInformation: ClaimantBankInformation) =>
      claimantBankInformation.financialDetails.awardEndDate.isAfter(today)

  val rollNumberIsDefined: ClaimantBankInformation => Boolean = (claimantBankInformation: ClaimantBankInformation) =>
    claimantBankInformation.financialDetails.bankAccountInformation.buildingSocietyRollNumber.isDefined
}
