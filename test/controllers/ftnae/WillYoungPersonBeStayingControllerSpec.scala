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
import forms.ftnae.WillYoungPersonBeStayingFormProvider
import models.common.{FirstForename, Surname}
import models.ftnae.{FtneaChildInfo, FtneaClaimantInfo, FtneaResponse}
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtneaResponseUserAnswer, WillYoungPersonBeStayingPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.WillYoungPersonBeStayingView

import scala.concurrent.Future

class WillYoungPersonBeStayingControllerSpec extends CBSpecBase with MockitoSugar {

  def onwardYesRoute = Call("GET", "/foo")
  def onwardNoRoute  = Call("GET", "/moo")

  val formProvider = new WillYoungPersonBeStayingFormProvider()
  val form         = formProvider("claimant-name")

  lazy val willYoungPersonBeStayingRoute =
    controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode).url

  "WillYoungPersonBeStaying Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, willYoungPersonBeStayingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WillYoungPersonBeStayingView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, NormalMode)(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WillYoungPersonBeStayingPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, willYoungPersonBeStayingRoute)

        val view = application.injector.instanceOf[WillYoungPersonBeStayingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(true), NormalMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to correct page when user enters yes and is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers(userAnswersId).set(WillYoungPersonBeStayingPage, true).success.value

      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardYesRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardYesRoute.url
      }
    }

    "must redirect to correct page when user enters no and is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers(userAnswersId).set(WillYoungPersonBeStayingPage, false).success.value

      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardNoRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardNoRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ftneaResponse =
        FtneaResponse(FtneaClaimantInfo(FirstForename("claimant-name"), Surname("")), List.empty[FtneaChildInfo])

      val userAnswers = UserAnswers(userAnswersId)
        .set(FtneaResponseUserAnswer, ftneaResponse)
        .success
        .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WillYoungPersonBeStayingView]

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
        val request = FakeRequest(GET, willYoungPersonBeStayingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
