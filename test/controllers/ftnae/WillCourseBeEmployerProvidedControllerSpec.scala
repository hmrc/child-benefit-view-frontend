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

import base.CBSpecBase
import forms.ftnae.WillCourseBeEmployerProvidedFormProvider
import models.ftnae.HowManyYears
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{HowManyYearsPage, SchoolOrCollegePage, WillCourseBeEmployerProvidedPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.WillCourseBeEmployerProvidedView

import scala.concurrent.Future

class WillCourseBeEmployerProvidedControllerSpec extends CBSpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new WillCourseBeEmployerProvidedFormProvider()
  val form         = formProvider()

  lazy val willCourseBeEmployerProvidedRoute =
    controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode).url

  "WillCourseBeEmployerProvided Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(HowManyYearsPage, HowManyYears.Oneyear).toOption

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

      val application =
        applicationBuilder(userAnswers)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, willCourseBeEmployerProvidedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WillCourseBeEmployerProvidedView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, NormalMode)(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WillCourseBeEmployerProvidedPage, true).success.value

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request = FakeRequest(GET, willCourseBeEmployerProvidedRoute)

        val view = application.injector.instanceOf[WillCourseBeEmployerProvidedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(true), NormalMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId).set(WillCourseBeEmployerProvidedPage, true).success.value

      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, willCourseBeEmployerProvidedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId).set(SchoolOrCollegePage, true).success.value

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, willCourseBeEmployerProvidedRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WillCourseBeEmployerProvidedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(boundForm, NormalMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, willCourseBeEmployerProvidedRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, willCourseBeEmployerProvidedRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}