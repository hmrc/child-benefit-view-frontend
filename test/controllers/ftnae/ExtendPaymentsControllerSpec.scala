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
import connectors.FtnaeConnector
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import models.errors.{CBError, FtnaeCannotFindYoungPersonError, FtnaeNoCHBAccountError}
import models.ftnae.{FtnaeChildInfo, FtnaeClaimantInfo, FtnaeResponse}
import models.pertaxAuth.PertaxAuthResponseModel
import models.{CBEnvelope, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.FtnaeResponseUserAnswer
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import views.html.ftnae.ExtendPaymentsView

import scala.concurrent.{ExecutionContext, Future}
import utils.TestData
import stubs.AuthStubs
import stubs.AuthStubs.mockPostPertaxAuth

class ExtendPaymentsControllerSpec extends BaseAppSpec with MockitoSugar with FtnaeFixture {

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val extendPaymentsRoute: String = controllers.ftnae.routes.ExtendPaymentsController.onPageLoad().url
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
  val mockFtnaeConnector:    FtnaeConnector    = mock[FtnaeConnector]

  "ExtendPayments Controller" - {
    "must return OK and the correct view for a GET, call the backend service, and store the result in session" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(FtnaeResponseUserAnswer, ftnaeResponse)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeConnector].toInstance(mockFtnaeConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))
      when(
        mockFtnaeConnector.getFtnaeAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ).thenReturn(CBEnvelope(ftnaeResponse))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExtendPaymentsView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(ftnaeResponse.claimant)(request, messages(application)).toString
        )
      }
    }

    "must redirect to No Account Found page, when backend service responds with FtnaeNoCHBAccountError" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeConnector].toInstance(mockFtnaeConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))

      when(
        mockFtnaeConnector.getFtnaeAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ).thenReturn(CBEnvelope.fromError[CBError, FtnaeResponse](FtnaeNoCHBAccountError))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.NoAccountFoundController.onPageLoad.url)
      }
    }

    "must redirect to CannotFindYoungPersonController page, when the backend service responds with Ftna" +
      "eCannotFindYoungPersonError" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeConnector].toInstance(mockFtnaeConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))
      when(
        mockFtnaeConnector.getFtnaeAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ).thenReturn(CBEnvelope.fromError[CBError, FtnaeResponse](FtnaeCannotFindYoungPersonError))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(
          controllers.ftnae.routes.CannotFindYoungPersonController.onPageLoad().url
        )
      }
    }
  }
}
