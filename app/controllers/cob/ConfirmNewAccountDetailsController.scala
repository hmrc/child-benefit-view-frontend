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
import forms.cob.ConfirmNewAccountDetailsFormProvider
import models.Mode
import models.cob.NewAccountDetails
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AuditService, ChangeOfBankService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import utils.navigation.Navigator
import views.html.cob.ConfirmNewAccountDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmNewAccountDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    featureActions:           FeatureFlagComposedActions,
    getData:                  CBDataRetrievalAction,
    requireData:              DataRequiredAction,
    formProvider:             ConfirmNewAccountDetailsFormProvider,
    changeOfBankService:      ChangeOfBankService,
    val controllerComponents: MessagesControllerComponents,
    view:                     ConfirmNewAccountDetailsView,
    errorHandler:             ErrorHandler
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen getData andThen requireData).async { implicit request =>
      changeOfBankService.retrieveBankClaimantInfo.fold(
        err => errorHandler.handleError(err),
        claimantInfo => {
          val preparedForm = request.userAnswers.get(ConfirmNewAccountDetailsPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }
          val changeAccount: Option[NewAccountDetails] = request.userAnswers.get(NewAccountDetailsPage)
          Ok(
            view(
              preparedForm,
              mode,
              s"${claimantInfo.firstForename.value} ${claimantInfo.surname.value}",
              changeAccount.get.newAccountHoldersName,
              changeAccount.get.newSortCode,
              changeAccount.get.newAccountNumber
            )
          )
        }
      )
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen getData andThen requireData).async { implicit request =>
      changeOfBankService.retrieveBankClaimantInfo.value.flatMap {
        case Left(err) => Future.successful(errorHandler.handleError(err))
        case Right(claimantInfo) => {
          val changeAccount: Option[NewAccountDetails] = request.userAnswers.get(NewAccountDetailsPage)
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      s"${claimantInfo.firstForename.value} ${claimantInfo.surname.value}",
                      changeAccount.get.newAccountHoldersName,
                      changeAccount.get.newSortCode,
                      changeAccount.get.newAccountNumber
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmNewAccountDetailsPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                  _ <-
                    changeOfBankService
                      .submitClaimantChangeOfBank(
                        claimantInfo.financialDetails.bankAccountInformation,
                        changeAccount
                      )
                      .value
                } yield Redirect(navigator.nextPage(ConfirmNewAccountDetailsPage, mode, updatedAnswers))
            )
        }
      }
    }
}
