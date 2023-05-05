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
import models.CBEnvelope
import models.common.ChildReferenceNumber
import models.errors.{CBError, ConnectorError}
import models.ftnae.{ChildDetails, CourseDuration, FtneaAuditAnswer}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FtnaeService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import views.html.ftnae.PaymentsExtendedView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class PaymentsExtendedControllerSpec extends BaseISpec with MockitoSugar with FtneaFixture {

  val mockFtnaeService      = mock[FtnaeService]
  val mockFtneaConnector    = mock[FtneaConnector]
  val mockSessionRepository = mock[SessionRepository]

  "PaymentsExtended Controller" - {

    "must return OK and the correct view for a GET" in {
      val childName = "John Doe"
      val childDetails =
        ChildDetails(CourseDuration.OneYear, ChildReferenceNumber("AA123456"),
          LocalDate.of(2001, 1, 1), List.empty[FtneaAuditAnswer])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .overrides(bind[FtnaeService].toInstance(mockFtnaeService))
        .overrides(bind[FtneaConnector].toInstance(mockFtneaConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))

      when(
        mockFtnaeService
          .submitFtnaeInformation(any[Option[List[SummaryListRow]]])(any[ExecutionContext](), any[HeaderCarrier](),
            any[DataRequest[AnyContent]]())
      ) thenReturn CBEnvelope(Right((childName, childDetails)))

      when(
        mockFtneaConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.PaymentsExtendedController.onPageLoad().url)

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
        .overrides(bind[FtneaConnector].toInstance(mockFtneaConnector))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(emptyUserAnswers))

      when(
        mockFtnaeService
          .submitFtnaeInformation(any[Option[List[SummaryListRow]]])(any[ExecutionContext](), any[HeaderCarrier](), any[DataRequest[AnyContent]]())
      ) thenReturn CBEnvelope.fromError[CBError, (String, ChildDetails)](ConnectorError(400, "some error"))

      when(
        mockFtneaConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope.fromError[CBError, Unit](ConnectorError(400, "some error"))

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.PaymentsExtendedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustEqual Some(controllers.routes.ServiceUnavailableController.onPageLoad.url)
      }
    }

  }
}
