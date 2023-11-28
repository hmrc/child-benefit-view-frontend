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

import base.BaseAppSpec
import forms.ftnae.HowManyYearsFormProvider
import models.ftnae.HowManyYears
import models.{CheckMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{HowManyYearsPage, TwelveHoursAWeekPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.HowManyYearsView

import scala.concurrent.Future

class HowManyYearsControllerSpec extends BaseAppSpec with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val howManyYearsRoute = controllers.ftnae.routes.HowManyYearsController.onPageLoad(CheckMode).url

  val formProvider = new HowManyYearsFormProvider()
  val form         = formProvider()

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  "HowManyYears Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers           = emptyUserAnswers.set(TwelveHoursAWeekPage, true).toOption
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, howManyYearsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HowManyYearsView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers           = UserAnswers(userAnswersId).set(HowManyYearsPage, HowManyYears.values.head).toOption
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

      val application = applicationBuilder(userAnswers = userAnswers)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, howManyYearsRoute)

        val view = application.injector.instanceOf[HowManyYearsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(HowManyYears.values.head), CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val userAnswers = emptyUserAnswers.set(HowManyYearsPage, HowManyYears.values.head).success.value

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
          FakeRequest(POST, howManyYearsRoute)
            .withFormUrlEncodedBody(("value", HowManyYears.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers.set(TwelveHoursAWeekPage, true).toOption

      val application = applicationBuilder(userAnswers = userAnswers)
        .build()
      running(application) {
        val request =
          FakeRequest(POST, howManyYearsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[HowManyYearsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(boundForm, CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to Service Unavailable for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, howManyYearsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ServiceUnavailableController.onPageLoad.url
      }
    }

    "redirect to Service Unavailable for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, howManyYearsRoute)
            .withFormUrlEncodedBody(("value", HowManyYears.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.ServiceUnavailableController.onPageLoad.url
      }
    }
  }
}
