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

import cats.data.EitherT
import controllers.actions._
import forms.cob.NewAccountDetailsFormProvider
import scala.concurrent.Future
import models.changeofbank.{AccountHolderName, BankAccountNumber, SortCode}
import models.cob.NewAccountDetails
import models.errors.CBError
import play.api.mvc.{Request, Result}
import repositories.SessionRepository
import services.ChangeOfBankService
import models.requests.OptionalDataRequest
import models.{Mode, UserAnswers}
import pages.cob.NewAccountDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.navigation.Navigator
import views.html.cob.NewAccountDetailsView
import cats.syntax.either._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext}

class NewAccountDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    featureActions:           FeatureFlagComposedActions,
    getData:                  CobDataRetrievalAction,
    changeOfBankService:      ChangeOfBankService,
    formProvider:             NewAccountDetailsFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view:                     NewAccountDetailsView
)(implicit ec:                ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen getData) { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(NewAccountDetailsPage) match {
        case None => form
        case Some(value) =>
          form
            .bind(
              Map(
                "newSortCode"           -> value.newSortCode,
                "newAccountHoldersName" -> value.newAccountHoldersName,
                "newAccountNumber"      -> value.newAccountNumber,
                "bacsError"             -> ""
              )
            )
      }

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.changeBankAction andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, mode)))
          },
          value => validateBankSaveSessionAndRedirect(mode, value)
        )
    }

  private def validateBankSaveSessionAndRedirect(mode: Mode, value: NewAccountDetails)(implicit
      request:                                         OptionalDataRequest[AnyContent]
  ): Future[Result] = {
    val result: EitherT[Future, CBError, Result] = validateBank(value, mode) {
      for {
        updatedAnswers <- Future.fromTry(
          request.userAnswers.getOrElse(UserAnswers(request.userId)).set(NewAccountDetailsPage, value)
        )
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(NewAccountDetailsPage, mode, updatedAnswers))
    }

    result.value.map(x => x.fold(_ => InternalServerError, r => r))
  }

  private def validateBank(value: NewAccountDetails, mode: Mode)(
      block:                      Future[Result]
  )(implicit request:             Request[_]):             EitherT[Future, CBError, Result] = {
    changeOfBankService
      .validate(
        AccountHolderName(value.newAccountHoldersName),
        SortCode(value.newSortCode),
        BankAccountNumber(value.newAccountNumber)
      )
      .flatMap(msg => {
        val foldedResult: Future[Result] = msg.fold({
          block
        })(x =>
          evaluateErrorResponse(x) {
            Future.successful {
              BadRequest(
                view(
                  form
                    .bind(
                      Map(
                        "newSortCode"           -> value.newSortCode,
                        "newAccountHoldersName" -> value.newAccountHoldersName,
                        "newAccountNumber"      -> value.newAccountNumber,
                        "bacsError"             -> x
                      )
                    ),
                  mode
                )
              )
            }
          }
        )
        EitherT(foldedResult.map(x => x.asRight[CBError]))
      })
  }
  private def evaluateErrorResponse(message: String)(block: => Future[Result]): Future[Result] =
    message match {
      case "The maximum number of retries reached when calling BAR" => {
        Future.successful(Redirect(routes.BARSLockOutController.onPageLoad))
      }
      case _ => block
    }
}
