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
import forms.ftnae.TwelveHoursAWeekFormProvider
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.ftnae.{FtnaeChildInfo, FtnaeClaimantInfo, FtnaeResponse}
import models.{CheckMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtnaeResponseUserAnswer, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.TwelveHoursAWeekView

import java.time.LocalDate
import scala.concurrent.Future
import utils.TestData
import stubs.AuthStubs

class TwelveHoursAWeekControllerSpec extends BaseAppSpec with MockitoSugar {

  def onwardYesRoute = Call("GET", "/foo")
  def onwardNoRoute  = Call("GET", "/moo")

  val formProvider = new TwelveHoursAWeekFormProvider()
  val form         = formProvider("First Name")

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val twelveHoursAWeekRoute = controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(CheckMode).url

  "TwelveHoursAWeek Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, twelveHoursAWeekRoute)
          .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TwelveHoursAWeekView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TwelveHoursAWeekPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, twelveHoursAWeekRoute)
          .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)
        val view = application.injector.instanceOf[TwelveHoursAWeekView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(true), CheckMode)(request, messages(application)).toString
        )
      }
    }

    "must redirect to the correct page when yes is selected" in {
      val userAnswers = emptyUserAnswers.set(SchoolOrCollegePage, false).success.value

      val mockSessionRepository = mock[SessionRepository]
      val updatedAnswers        = userAnswers.set(TwelveHoursAWeekPage, true).success.value
      when(mockSessionRepository.set(updatedAnswers)) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardYesRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, twelveHoursAWeekRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardYesRoute.url
      }
    }

    "must redirect to the correct page when no is selected" in {
      val userAnswers = emptyUserAnswers.set(SchoolOrCollegePage, false).success.value

      val mockSessionRepository = mock[SessionRepository]
      val updatedAnswers        = userAnswers.set(TwelveHoursAWeekPage, false).success.value
      when(mockSessionRepository.set(updatedAnswers)) thenReturn Future.successful(false)

      val application =
        applicationBuilder(userAnswers = Some(updatedAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardNoRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, twelveHoursAWeekRoute)
            .withFormUrlEncodedBody(("value", "false"))
            .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardNoRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val ftnaeResponse = FtnaeResponse(
        FtnaeClaimantInfo(FirstForename("s"), Surname("sa")),
        List(
          FtnaeChildInfo(
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
        .flatMap(x => x.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, twelveHoursAWeekRoute)
            .withFormUrlEncodedBody(("value", ""))
            .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TwelveHoursAWeekView]

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
        val request = FakeRequest(GET, twelveHoursAWeekRoute)
          .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ServiceUnavailableController.onPageLoad.url
      }
    }

    "must redirect to Service Unavailable for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, twelveHoursAWeekRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession("authToken" -> "Bearer 123")
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ServiceUnavailableController.onPageLoad.url
      }
    }
  }
}
