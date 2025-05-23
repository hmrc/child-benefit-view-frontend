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
import forms.ftnae.WhichYoungPersonFormProvider
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.ftnae.{FtnaeChildInfo, FtnaeClaimantInfo, FtnaeResponse}
import models.pertaxAuth.PertaxAuthResponseModel
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import stubs.AuthStubs
import stubs.AuthStubs.*
import uk.gov.hmrc.govukfrontend.views.Aliases.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData
import utils.TestData.ninoUser
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.WhichYoungPersonView

import scala.concurrent.Future

class WhichYoungPersonControllerSpec extends BaseAppSpec with MockitoSugar with FtnaeFixture {

  def onwardRoute: Call = Call("GET", "/foo")

  def whichYoungPersonRoute(mode: Mode): String =
    controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(mode).url

  val formProvider = new WhichYoungPersonFormProvider()
  val form:                     Form[String] = formProvider()
  lazy val extendPaymentsRoute: String       = controllers.ftnae.routes.ExtendPaymentsController.onPageLoad().url

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private def arrangeRadioButtons(
      ftnaeResponseUserAnswer:   FtnaeResponse
  )(youngPersonNotListedMessage: String): List[RadioItem] = {
    val initialOrder: List[(String, Int)] = (youngPersonNotListedMessage :: ftnaeResponseUserAnswer.children
      .map(c => {
        val midName = c.midName.map(mn => s"${mn.value} ").getOrElse("")
        s"${c.name.value} $midName${c.lastName.value}"
      })).zipWithIndex

    val childNotListedMessage = initialOrder.head
    val restOfTheList         = initialOrder.tail

    val orderedWithIndex0InTheEnd = restOfTheList ::: List(childNotListedMessage)
    orderedWithIndex0InTheEnd.map { x =>
      val value = if (x._1 == youngPersonNotListedMessage) x._2.toString else x._1
      RadioItem(content = Text(x._1), value = Some(value), id = Some(s"value_${x._2}"))
    }
  }

  val ftnaeResponse: FtnaeResponse = FtnaeResponse(
    FtnaeClaimantInfo(FirstForename("s"), Surname("sa")),
    List(
      FtnaeChildInfo(
        ChildReferenceNumber("crn1234"),
        FirstForename("First Name"),
        None,
        Surname("Surname"),
        sixteenBy1stOfSeptemberThisYear,
        getFirstMondayOfSeptemberThisYear
      )
    )
  )

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  "WhichYoungPerson Controller" - {

    "must redirect to Service unavailable if no answers are returned" in {

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(None))

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
      userLoggedInIsChildBenefitUser(ninoUser)

      running(application) {
        val request = FakeRequest(GET, whichYoungPersonRoute(NormalMode))
          .withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          ""
        )

      }

    }

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(FtnaeResponseUserAnswer, ftnaeResponse)
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
      userLoggedInIsChildBenefitUser(ninoUser)

      running(application) {
        val request = FakeRequest(GET, whichYoungPersonRoute(NormalMode))
          .withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhichYoungPersonView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, NormalMode, arrangeRadioButtons(ftnaeResponse), ftnaeResponse)(
            request,
            messages(application)
          ).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First Name Surname")
        .flatMap(x => x.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

      running(application) {
        val request =
          FakeRequest(GET, whichYoungPersonRoute(NormalMode))
            .withFormUrlEncodedBody(("value", "First Name Surname"))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)
        val view = application.injector.instanceOf[WhichYoungPersonView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill("First Name Surname"), NormalMode, arrangeRadioButtons(ftnaeResponse), ftnaeResponse)(
            request,
            messages(application)
          ).toString
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First Name Surname")
        .flatMap(x => x.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whichYoungPersonRoute(NormalMode))
            .withFormUrlEncodedBody(("value", "First Name Surname"))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must set name changed in user answers if in check mode" in {
      val mockSessionRepository = mock[SessionRepository]
      val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First Name Surname")
        .flatMap(x => x.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(captor.capture())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whichYoungPersonRoute(CheckMode))
            .withFormUrlEncodedBody(("value", "A Different Name"))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        await(route(application, request).value)

        captor.getValue.nameChangedDuringCheck mustEqual true
      }
    }

    "must not set name changed in user answers if in check mode but name not changed" in {
      val mockSessionRepository = mock[SessionRepository]
      val captor                = ArgumentCaptor.forClass(classOf[UserAnswers])
      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First Name Surname")
        .flatMap(x => x.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
      when(mockSessionRepository.set(captor.capture())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whichYoungPersonRoute(CheckMode))
            .withFormUrlEncodedBody(("value", "First Name Surname"))
            .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        await(route(application, request).value)

        captor.getValue.nameChangedDuringCheck mustEqual false
      }
    }

  }
}
