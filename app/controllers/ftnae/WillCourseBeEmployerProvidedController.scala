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

package controllers.ftnae

import controllers.actions._
import forms.ftnae.WillCourseBeEmployerProvidedFormProvider
import models.Mode
import models.requests.DataRequest
import pages.ftnae.{FtneaResponseUserAnswer, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.navigation.Navigator
import views.html.ftnae.WillCourseBeEmployerProvidedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WillCourseBeEmployerProvidedController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    identify:                 IdentifierAction,
    getData:                  CBDataRetrievalAction,
    requireData:              DataRequiredAction,
    formProvider:             WillCourseBeEmployerProvidedFormProvider,
    val controllerComponents: MessagesControllerComponents,
    featureActions:           FeatureFlagComposedActions,
    view:                     WillCourseBeEmployerProvidedView
)(implicit ec:                ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def form[A](implicit request: DataRequest[A]) = {
    val displayName = request.userAnswers.get(FtneaResponseUserAnswer) match {
      case None       => "N/A"
      case Some(item) => item.claimant.name.value
    }
    formProvider(displayName)
  }
  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(WillCourseBeEmployerProvidedPage) match {
        case None =>
          form

        case Some(value) =>
          form.fill(value)

      }

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WillCourseBeEmployerProvidedPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WillCourseBeEmployerProvidedPage, mode, updatedAnswers))
        )
    }
}
