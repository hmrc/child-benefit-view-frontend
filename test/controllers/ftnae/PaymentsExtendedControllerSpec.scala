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
import models.CBEnvelope
import models.common.ChildReferenceNumber
import models.errors.{CBError, ConnectorError}
import models.ftnae.{ChildDetails, CourseDuration, FtnaeQuestionAndAnswer}
import models.pertaxAuth.PertaxAuthResponseModel
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FtnaeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeNonce
import views.html.ftnae.PaymentsExtendedView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import stubs.AuthStubs
import stubs.AuthStubs.mockPostPertaxAuth
import utils.TestData

class PaymentsExtendedControllerSpec extends BaseAppSpec with MockitoSugar with FtnaeFixture {

  val mockFtnaeService:      FtnaeService      = mock[FtnaeService]
  val mockFtnaeConnector:    FtnaeConnector    = mock[FtnaeConnector]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  "PaymentsExtended Controller" - {

    "must return OK and the correct view for a GET" in {
      val childName = "John Doe"
      val childDetails =
        ChildDetails(
          CourseDuration.OneYear,
          ChildReferenceNumber("AA123456"),
          LocalDate.of(2001, 1, 1),
          "sample-name",
          List.empty[FtnaeQuestionAndAnswer]
        )

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeService].toInstance(mockFtnaeService))
        .overrides(bind[FtnaeConnector].toInstance(mockFtnaeConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))

      when(
        mockFtnaeService
          .submitFtnaeInformation(any[Option[List[SummaryListRow]]])(
            any[ExecutionContext](),
            any[HeaderCarrier](),
            any[DataRequest[AnyContent]](),
            any[Messages]
          )
      ).thenReturn(CBEnvelope(Right((childName, childDetails))))

      when(
        mockFtnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ).thenReturn(CBEnvelope(()))

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.PaymentsExtendedController.onPageLoad().url)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentsExtendedView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(childName, childDetails.courseDuration)(request, messages(application)).toString
        )
      }
    }

    "must redirect to service unavailable page if failure occurs" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeService].toInstance(mockFtnaeService))
        .overrides(bind[FtnaeConnector].toInstance(mockFtnaeConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(emptyUserAnswers)))

      when(
        mockFtnaeService
          .submitFtnaeInformation(any[Option[List[SummaryListRow]]])(
            any[ExecutionContext](),
            any[HeaderCarrier](),
            any[DataRequest[AnyContent]](),
            any[Messages]
          )
      ).thenReturn(CBEnvelope.fromError[CBError, (String, ChildDetails)](ConnectorError(400, "some error")))

      when(
        mockFtnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ).thenReturn(CBEnvelope.fromError[CBError, Unit](ConnectorError(400, "some error")))

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.PaymentsExtendedController.onPageLoad().url)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.ServiceUnavailableController.onPageLoad.url)
      }
    }

  }
}
