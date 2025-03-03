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
import forms.ftnae.WillYoungPersonBeStayingFormProvider
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.ftnae.{FtnaeChildInfo, FtnaeClaimantInfo, FtnaeResponse}
import models.pertaxAuth.PertaxAuthResponseModel
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage, WillYoungPersonBeStayingPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.WillYoungPersonBeStayingView

import java.time.LocalDate
import scala.concurrent.Future
import utils.TestData
import stubs.AuthStubs
import stubs.AuthStubs.mockPostPertaxAuth

class WillYoungPersonBeStayingControllerSpec extends BaseAppSpec with MockitoSugar {

  def onwardYesRoute: Call = Call("GET", "/foo")

  def onwardNoRoute: Call = Call("GET", "/moo")

  val formProvider = new WillYoungPersonBeStayingFormProvider()
  val form: Form[Boolean] = formProvider("First Name")

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val willYoungPersonBeStayingRoute: String =
    controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode).url

  "WillYoungPersonBeStaying Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, willYoungPersonBeStayingRoute)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

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
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

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

      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

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
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardYesRoute.url
      }
    }

    "must redirect to correct page when user enters no and is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers           = UserAnswers(userAnswersId).set(WillYoungPersonBeStayingPage, false).success.value

      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

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
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
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
        .toOption

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", ""))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

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

    "must redirect to Service Unavailable for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, willYoungPersonBeStayingRoute)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
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
          FakeRequest(POST, willYoungPersonBeStayingRoute)
            .withFormUrlEncodedBody(("value", "true"))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ServiceUnavailableController.onPageLoad.url
      }
    }
  }
}
