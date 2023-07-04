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
import forms.ftnae.WhichYoungPersonFormProvider
import models.ftnae.FtnaeResponse
import models.{Mode, CheckMode, UserAnswers}
import models.requests.OptionalDataRequest
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.helpers.StringHelper.toFtnaeChildNameTitleCase
import utils.navigation.Navigator
import views.html.ftnae.WhichYoungPersonView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WhichYoungPersonController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository:        SessionRepository,
    navigator:                Navigator,
    identify:                 IdentifierAction,
    getData:                  CBDataRetrievalAction,
    formProvider:             WhichYoungPersonFormProvider,
    val controllerComponents: MessagesControllerComponents,
    featureActions:           FeatureFlagComposedActions,
    view:                     WhichYoungPersonView
)(implicit ec:                ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData) { implicit request =>
      val userAnswers: UserAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

      val preparedForm = userAnswers.get(WhichYoungPersonPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      userAnswers.get(FtnaeResponseUserAnswer) match {
        case Some(ftnaeResponseUserAnswer) =>
          Ok(
            view(
              preparedForm,
              mode,
              arrangeRadioButtons(ftnaeResponseUserAnswer),
              ftnaeResponseUserAnswer
            )
          )
        case None =>
          Redirect(controllers.routes.ServiceUnavailableController.onPageLoad)

      }

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData).async { implicit request =>
      {
        val userAnswers: UserAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))

        val ftnaeResponseUserAnswer: Option[FtnaeResponse] =
          userAnswers.get(FtnaeResponseUserAnswer)

        ftnaeResponseUserAnswer.fold(
          Future.successful(Redirect(controllers.routes.ServiceUnavailableController.onPageLoad))
        )(answer => {
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, mode, arrangeRadioButtons(answer), answer)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(updateName(request, mode, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(WhichYoungPersonPage, mode, updatedAnswers))
            )
        })
      }
    }

  private def updateName(request: OptionalDataRequest[AnyContent], mode: Mode, value: String): Try[UserAnswers] = {
    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
    val qypChanged  = mode == CheckMode && userAnswers.get(WhichYoungPersonPage) != Some(value)
    userAnswers
      .copy(nameChangedDuringCheck = qypChanged)
      .set(WhichYoungPersonPage, value)
  }

  private def arrangeRadioButtons(
      ftnaeResponseUserAnswer:   FtnaeResponse
  )(youngPersonNotListedMessage: String): List[RadioItem] = {
    val initialOrder: List[(String, Int)] = (youngPersonNotListedMessage :: (
      ftnaeResponseUserAnswer.children
        .map(c => {
          toFtnaeChildNameTitleCase(c)
        })
      )).zipWithIndex.toList

    val childNotListedMessage = initialOrder.head
    val restOfTheList         = initialOrder.tail

    val orderedWithIndex0InTheEnd = restOfTheList ::: List(childNotListedMessage)
    orderedWithIndex0InTheEnd.map(x => {
      val value = if (x._1 == youngPersonNotListedMessage) x._2.toString else x._1
      RadioItem(content = Text(x._1), value = Some(value), id = Some(s"value_${x._2}"))
    })
  }
}
