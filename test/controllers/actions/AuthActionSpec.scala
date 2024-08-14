/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.mvc.{BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import base.BaseAppSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import stubs.AuthStubs
import utils.TestData
import utils.enrolments.HmrcPTChecks

class AuthActionSpec extends BaseAppSpec {
  lazy val mockHmrcPTChecks = mock[HmrcPTChecks]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad() = authAction { _ => Results.Ok }
  }

  "Auth Action" - {
    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {
        when(mockHmrcPTChecks.isHmrcPTEnrolmentPresentAndValid(any(), any())).thenReturn(true)

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            appConfig,
            bodyParsers,
            mockHmrcPTChecks
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {
        when(mockHmrcPTChecks.isHmrcPTEnrolmentPresentAndValid(any(), any())).thenReturn(true)

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            appConfig,
            bodyParsers,
            mockHmrcPTChecks
          )
          val controller = new Harness(authAction)
          val result     = controller.onPageLoad()(FakeRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "th users has no Nino in retrievals" - {
      "must redirect the user to the unauthorised page" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val authAction = application.injector.instanceOf[AuthenticatedIdentifierAction]
          AuthStubs.userLoggedInIsChildBenefitUser(TestData.userWithNoNino)
          val controller = new Harness(authAction)
          val req        = FakeRequest().withSession("authToken" -> "Bearer 123")
          val result     = controller.onPageLoad()(req)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must be(controllers.routes.UnauthorisedController.onPageLoad.url)
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
      hc:                              HeaderCarrier,
      ec:                              ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
