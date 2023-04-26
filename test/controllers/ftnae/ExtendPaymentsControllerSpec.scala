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

import connectors.FtneaConnector
import models.{CBEnvelope, UserAnswers}
import models.common.{FirstForename, Surname}
import models.errors.{CBError, FtneaCannotFindYoungPersonError, FtneaNoCHBAccountError}
import models.ftnae.{Crn, FtneaChildInfo, FtneaClaimantInfo, FtneaResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.FtneaResponseUserAnswer
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.inject.bind
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import views.html.ftnae.ExtendPaymentsView

import scala.concurrent.{ExecutionContext, Future}

class ExtendPaymentsControllerSpec extends BaseISpec with MockitoSugar with FtneaFixture {

  def onwardRoute = Call("GET", "/foo")

  lazy val extendPaymentsRoute = controllers.ftnae.routes.ExtendPaymentsController.onPageLoad.url
  val ftneaResponse = FtneaResponse(
    FtneaClaimantInfo(FirstForename("s"), Surname("sa")),
    List(
      FtneaChildInfo(
        Crn("crn1234"),
        FirstForename("First Name"),
        None,
        Surname("Surname"),
        sixteenBy1stOfSeptemberThisYear,
        getFirstMondayOfSeptemberThisYear()
      )
    )
  )
  val mockSessionRepository = mock[SessionRepository]
  val mockFtneaConnector    = mock[FtneaConnector]

  "ExtendPayments Controller" - {
    "must return OK and the correct view for a GET, call the backend service, and store the result in session" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(FtneaResponseUserAnswer, ftneaResponse)
        .success
        .value
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtneaConnector].toInstance(mockFtneaConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)
      when(
        mockFtneaConnector.getFtneaAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn (CBEnvelope(ftneaResponse))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExtendPaymentsView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(ftneaResponse.claimant)(request, messages(application)).toString
        )
      }
    }

    "must redirect to No Account Found page, when backend service responds with FtneaNoCHBAccountError" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtneaConnector].toInstance(mockFtneaConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))

      when(
        mockFtneaConnector.getFtneaAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn (CBEnvelope.fromError[CBError, FtneaResponse](FtneaNoCHBAccountError))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.NoAccountFoundController.onPageLoad.url)
      }
    }

    "must redirect to CannotFindYoungPersonController page, when the backend service responds with FtneaCannotFindYoungPersonError" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtneaConnector].toInstance(mockFtneaConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))
      when(
        mockFtneaConnector.getFtneaAccountDetails()(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn (CBEnvelope.fromError[CBError, FtneaResponse](FtneaCannotFindYoungPersonError))

      running(application) {
        val request = FakeRequest(GET, extendPaymentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.ftnae.routes.CannotFindYoungPersonController.onPageLoad.url)
      }
    }
  }
}
