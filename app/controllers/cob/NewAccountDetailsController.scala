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

package controllers.cob

import controllers.actions._
import forms.cob.NewAccountDetailsFormProvider

import scala.concurrent.Future
import models.changeofbank.{AccountHolderName, BankAccountNumber, SortCode}
import models.cob.{NewAccountDetails, WhatTypeOfAccount}
import models.errors._
import play.api.mvc.{Request, Result}
import repositories.SessionRepository
import services.{AuditService, ChangeOfBankService}
import models.requests.OptionalDataRequest
import models.{Mode, UserAnswers}
import pages.cob.{NewAccountDetailsPage, WhatTypeOfAccountPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.filters.csrf.CSRF
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import utils.navigation.Navigator
import views.html.cob.NewAccountDetailsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NewAccountDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    featureActions:           FeatureFlagComposedActions,
    getData:                  CBDataRetrievalAction,
    changeOfBankService:      ChangeOfBankService,
    formProvider:             NewAccountDetailsFormProvider,
    verifyBarNotLockedAction: VerifyBarNotLockedAction,
    verifyHICBCAction:        VerifyHICBCAction,
    val controllerComponents: MessagesControllerComponents,
    view:                     NewAccountDetailsView,
    errorHandler:             ErrorHandler
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = {
    (featureActions.changeBankAction andThen verifyBarNotLockedAction andThen verifyHICBCAction andThen getData) {
      implicit request =>
        val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(NewAccountDetailsPage) match {
          case None => form
          case Some(value) =>
            bindForm(value, None)
        }

        getUserAccountType(request) match {
          case Right(accountType) => Ok(view(preparedForm, mode, accountType))
          case Left(err)          => errorHandler.handleError(err)
        }
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen verifyBarNotLockedAction andThen verifyHICBCAction andThen getData).async {
      implicit request =>
        getUserAccountType(request) match {
          case Right(accountType) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {
                  Future.successful(BadRequest(view(formWithErrors, mode, accountType)))
                },
                value => validateBankSaveSessionAndRedirect(mode, value)
              )
          case Left(err) =>
            Future.successful(errorHandler.handleError(err))
        }
    }

  private def getUserAccountType(
      request: OptionalDataRequest[AnyContent]
  ): Either[CBError, WhatTypeOfAccount] = {
    request.userAnswers
      .flatMap(_.get(WhatTypeOfAccountPage))
      .toRight(ChangeOfBankValidationError(BAD_REQUEST, message = "Missing account type"))
  }

  private def validateBankSaveSessionAndRedirect(mode: Mode, value: NewAccountDetails)(implicit
      request:                                         OptionalDataRequest[AnyContent]
  ): Future[Result] = {
    validateBank(value, mode) {
      bindForm(value, None)
      for {
        updatedAnswers <- Future.fromTry(
          request.userAnswers.getOrElse(UserAnswers(request.userId)).set(NewAccountDetailsPage, value)
        )
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(NewAccountDetailsPage, mode, updatedAnswers))
    }
  }

  private def validateBank(value: NewAccountDetails, mode:          Mode)(
      successBlock:               => Future[Result]
  )(implicit request:             OptionalDataRequest[AnyContent]): Future[Result] = {

    changeOfBankService
      .validate(
        AccountHolderName(value.newAccountHoldersName),
        SortCode(value.newSortCode),
        BankAccountNumber(value.newAccountNumber)
      )
      .foldF(
        error =>
          evaluateErrorResponse(error)(
            lockedOutRedirect(),
            bacsErrorBlock(value, mode, error),
            noBacsRelatedError
          ),
        _ => successBlock
      )

  }

  private def evaluateErrorResponse(error: CBError)(
      lockedOutRedirect:                   => Future[Result],
      verificationError:                   => Future[Result],
      noBacsRelatedError:                  CBError => Future[Result]
  ): Future[Result] =
    error match {
      case ClaimantIsLockedOutOfChangeOfBank(_, _) => lockedOutRedirect
      case PriorityBacsVerificationError(_, _)     => verificationError
      case e                                       => noBacsRelatedError(e)
    }

  private def lockedOutRedirect(): Future[Result] =
    Future.successful(Redirect(routes.CannotVerifyAccountController.onPageLoad()))

  private def noBacsRelatedError(e: CBError)(implicit request: Request[_]) =
    Future.successful(errorHandler.handleError(e))

  private def bacsErrorBlock(value: NewAccountDetails, mode: Mode, error: CBError)(implicit
      request:                      OptionalDataRequest[AnyContent]
  ) =
    getUserAccountType(request) match {
      case Right(accountType) =>
        Future.successful {
          BadRequest(
            view(
              bindForm(value, Some(error.message)),
              mode,
              accountType
            )
          )
        }
      case Left(err) =>
        Future.successful(errorHandler.handleError(err))
    }

  private def bindForm(value: NewAccountDetails, bacsError: Option[String])(implicit
      request:                OptionalDataRequest[AnyContent]
  ): Form[NewAccountDetails] = {
    form
      .bind(
        Map(
          "newSortCode"           -> value.newSortCode,
          "newAccountHoldersName" -> value.newAccountHoldersName,
          "newAccountNumber"      -> value.newAccountNumber,
          "bacsError"             -> bacsError.getOrElse(""),
          "csrfToken"             -> CSRF.getToken.get.value
        )
      )
  }
}
