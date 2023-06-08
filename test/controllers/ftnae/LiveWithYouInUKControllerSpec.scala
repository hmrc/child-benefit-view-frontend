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
import forms.ftnae.LiveWithYouInUKFormProvider
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.ftnae.{FtneaChildInfo, FtneaClaimantInfo, FtneaResponse}
import models.{CheckMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtneaResponseUserAnswer, LiveWithYouInUKPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.LiveWithYouInUKView

import java.time.LocalDate
import scala.concurrent.Future

class LiveWithYouInUKControllerSpec extends CBSpecBase with MockitoSugar {

  def onwardYesRoute = Call("GET", "/foo")
  def onwardNoRoute  = Call("GET", "/moo")

  val formProvider = new LiveWithYouInUKFormProvider()
  val form         = formProvider("First Name")

  lazy val liveWithYouInUKRoute = controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(CheckMode).url

  "LiveWithYouInUK Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(WillCourseBeEmployerProvidedPage, true).toOption

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, liveWithYouInUKRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[LiveWithYouInUKView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(LiveWithYouInUKPage, true).success.value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, liveWithYouInUKRoute)

        val view = application.injector.instanceOf[LiveWithYouInUKView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(true), CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to the next page when yes is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId).set(LiveWithYouInUKPage, true).success.value

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
          FakeRequest(POST, liveWithYouInUKRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardYesRoute.url
      }

    }

    "must redirect to the next page when no is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = UserAnswers(userAnswersId).set(LiveWithYouInUKPage, false).success.value

      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(false)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardNoRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, liveWithYouInUKRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardNoRoute.url
      }

    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ftneaResponse = FtneaResponse(
        FtneaClaimantInfo(FirstForename("s"), Surname("sa")),
        List(
          FtneaChildInfo(
            ChildReferenceNumber("crn1234"),
            FirstForename("First Name"),
            None,
            Surname("Surname"),
            LocalDate.now(),
            LocalDate.now()
          )
        )
      )

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First Name Surname")
        .flatMap(x => x.set(FtneaResponseUserAnswer, ftneaResponse))
        .success
        .value

      val application = applicationBuilder(Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, liveWithYouInUKRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[LiveWithYouInUKView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(boundForm, CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, liveWithYouInUKRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, liveWithYouInUKRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
