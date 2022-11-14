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

package controllers

import base.CobSpecBase
import forms.cob.ConfirmNewAccountDetailsFormProvider
import models.cob.ConfirmNewAccountDetails.Yes
import models.cob.{ConfirmNewAccountDetails, NewAccountDetails}
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.cob.ConfirmNewAccountDetailsView
import scala.concurrent.Future
import org.scalatestplus.mockito.MockitoSugar
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

class ConfirmNewAccountDetailsControllerSpec extends CobSpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val confirmNewAccountDetailsRoute =
    controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode).url

  val formProvider = new ConfirmNewAccountDetailsFormProvider()
  val form         = formProvider()

  val newAccountDetails = NewAccountDetails("name", "123456", "11110000")

  "ConfirmNewAccountDetails Controller" - {

    "must return OK and the correct view for a GET" in {
      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).toOption
      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, confirmNewAccountDetailsRoute)
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(
            form,
            NormalMode,
            newAccountDetails.newAccountHoldersName,
            newAccountDetails.newSortCode,
            newAccountDetails.newAccountNumber
          )(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        .set(NewAccountDetailsPage, newAccountDetails)
        .flatMap(ua => ua.set(ConfirmNewAccountDetailsPage, ConfirmNewAccountDetails.values.head))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, confirmNewAccountDetailsRoute)
        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))

        val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(
            form.fill(ConfirmNewAccountDetails.values.head),
            NormalMode,
            newAccountDetails.newAccountHoldersName,
            newAccountDetails.newSortCode,
            newAccountDetails.newAccountNumber
          )(request, messages(application)).toString
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val confirmNewAccountDetails = Yes
      val userAnswers = UserAnswers(userAnswersId)
        .set(NewAccountDetailsPage, newAccountDetails)
        .flatMap(_.set(ConfirmNewAccountDetailsPage, confirmNewAccountDetails))
        .toOption

      val mockSessionRepository = mock[SessionRepository]

      val application =
        applicationBuilder(userAnswers = userAnswers)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, confirmNewAccountDetailsRoute)
            .withFormUrlEncodedBody(("value", ConfirmNewAccountDetails.values.head.toString))

        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)
        when(mockSessionRepository.set(userAnswers.get)) thenReturn Future.successful(true)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId)
        .set(NewAccountDetailsPage, newAccountDetails)
        .toOption

      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

      running(application) {
        val request =
          FakeRequest(POST, confirmNewAccountDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val result    = route(application, request).value
        val boundForm = form.bind(Map("value" -> "invalid value"))
        status(result) mustEqual BAD_REQUEST
        val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(
            boundForm,
            NormalMode,
            newAccountDetails.newAccountHoldersName,
            newAccountDetails.newSortCode,
            newAccountDetails.newAccountNumber
          )(request, messages(application)).toString
        )
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, confirmNewAccountDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, confirmNewAccountDetailsRoute)
            .withFormUrlEncodedBody(("value", ConfirmNewAccountDetails.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
