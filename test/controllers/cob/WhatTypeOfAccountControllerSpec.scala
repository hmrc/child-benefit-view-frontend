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

package controllers.cob

import base.BaseAppSpec
import forms.cob.WhatTypeOfAccountFormProvider
import models.cob.WhatTypeOfAccount.JointHeldByClaimant
import models.cob.WhatTypeOfAccount
import models.pertaxAuth.PertaxAuthResponseModel
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cob.WhatTypeOfAccountPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}
import repositories.SessionRepository
import testconfig.TestConfig
import testconfig.TestConfig._
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.navigation.{FakeNavigator, Navigator}
import stubs.AuthStubs._
import stubs.ChildBenefitServiceStubs._
import utils.TestData.ninoUser
import views.html.cob.WhatTypeOfAccountView

import scala.concurrent.{ExecutionContext, Future}

class WhatTypeOfAccountControllerSpec extends BaseAppSpec with MockitoSugar {

  implicit val mockExecutionContext: ExecutionContext = mock[ExecutionContext]
  implicit val mockHeaderCarrier:    HeaderCarrier    = mock[HeaderCarrier]

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  def onwardRoute: Call = Call("GET", "/foo")

  lazy val whatTypeOfAccountRoute: String =
    controllers.cob.routes.WhatTypeOfAccountController.onPageLoad(NormalMode).url

  val userAnswers: UserAnswers = UserAnswers(userAnswersId)

  val formProvider = new WhatTypeOfAccountFormProvider()
  val form: Form[WhatTypeOfAccount] = formProvider()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset[Object](
      mockSessionRepository,
      mockExecutionContext,
      mockHeaderCarrier
    )
  }

  "WhatTypeOfAccount Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must respond OK and the correct view for GET" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilderWithVerificationActions(config, userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, whatTypeOfAccountRoute)
            .withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[WhatTypeOfAccountView]

          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(form, NormalMode)(request, messages(application)).toString
          )
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(WhatTypeOfAccountPage, JointHeldByClaimant)
          .success
          .value

        val application = applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, whatTypeOfAccountRoute)
            .withSession("authToken" -> "Bearer 123")

          when(mockSessionRepository.get(userAnswersId))
            .thenReturn(Future.successful(Some(userAnswers)))

          val view = application.injector.instanceOf[WhatTypeOfAccountView]

          val result = route(application, request).value

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              form.fill(JointHeldByClaimant),
              NormalMode
            )(request, messages(application)).toString
          )
        }
      }

      "must return BAD_REQUEST for a POST if no existing data is found" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilderWithVerificationActions(config, userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, whatTypeOfAccountRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must redirect to the next page when a valid what type of account is submitted" - {
        WhatTypeOfAccount.values.foreach { wTOA =>
          s"What Type of Account: ${wTOA.toString}" in {
            mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
            userLoggedInIsChildBenefitUser(ninoUser)
            verifyClaimantBankAccountStub()
            val mockSessionRepository = mock[SessionRepository]

            val expectedUserAnswers: UserAnswers = UserAnswers(userAnswersId)
              .set(WhatTypeOfAccountPage, wTOA)
              .success
              .value

            val application =
              applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswers))
                .overrides(
                  bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

            running(application) {
              val request =
                CSRFTokenHelper.addCSRFToken(
                  FakeRequest(POST, whatTypeOfAccountRoute)
                    .withFormUrlEncodedBody(
                      ("value" -> wTOA.toString)
                    )
                    .withSession("authToken" -> "Bearer 123")
                )
              when(mockSessionRepository.get(userAnswersId))
                .thenReturn(Future.successful(Some(userAnswers)))

              when(mockSessionRepository.set(userAnswers.copy(data = expectedUserAnswers.data)))
                .thenReturn(Future.successful(true))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustBe onwardRoute.url
            }
          }
        }
      }

      "must fail to validate when no what type of account is selected" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)
        verifyClaimantBankAccountStub()
        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            CSRFTokenHelper.addCSRFToken(
              FakeRequest(POST, whatTypeOfAccountRoute)
                .withFormUrlEncodedBody()
                .withSession("authToken" -> "Bearer 123")
            )
          when(mockSessionRepository.get(userAnswersId))
            .thenReturn(Future.successful(Some(userAnswers)))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }
}
