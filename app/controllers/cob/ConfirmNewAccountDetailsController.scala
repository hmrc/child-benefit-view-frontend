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

package controllers.cob

import controllers.actions._
import forms.cob.ConfirmNewAccountDetailsFormProvider
import models.Mode
import models.cob.AccountDetails
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.navigation.Navigator
import views.html.cob.ConfirmNewAccountDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmNewAccountDetailsController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    identify:                 IdentifierAction,
    getData:                  CobDataRetrievalAction,
    requireData:              DataRequiredAction,
    formProvider:             ConfirmNewAccountDetailsFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view:                     ConfirmNewAccountDetailsView
)(implicit ec:                ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ConfirmNewAccountDetailsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      val changeAccount: Option[AccountDetails] = request.userAnswers.get(NewAccountDetailsPage)
      Ok(
        view(
          preparedForm,
          mode,
          changeAccount.get.accountHoldersName,
          changeAccount.get.sortCode,
          changeAccount.get.accountNumber
        )
      )
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val changeAccount: Option[AccountDetails] = request.userAnswers.get(NewAccountDetailsPage)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  changeAccount.get.accountHoldersName,
                  changeAccount.get.sortCode,
                  changeAccount.get.accountNumber
                )
              )
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmNewAccountDetailsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ConfirmNewAccountDetailsPage, mode, updatedAnswers))
        )
    }
}
