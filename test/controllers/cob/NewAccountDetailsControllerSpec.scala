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
import controllers.actions.{FakeRedirectToPegaAction, FakeVerifyBarNotLockedAction, FakeVerifyHICBCAction}
import forms.cob.NewAccountDetailsFormProvider
import models.cob.{NewAccountDetails, WhatTypeOfAccount}
import models.pertaxAuth.PertaxAuthResponseModel
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.cob.{NewAccountDetailsPage, WhatTypeOfAccountPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.*
import play.api.test.{CSRFTokenHelper, FakeRequest}
import repositories.SessionRepository
import stubs.AuthStubs.*
import stubs.ChildBenefitServiceStubs.*
import testconfig.TestConfig
import testconfig.TestConfig.*
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData.{lockedOutErrorResponse, ninoUser}
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ErrorTemplate
import views.html.cob.NewAccountDetailsView

import scala.concurrent.Future

class NewAccountDetailsControllerSpec extends BaseAppSpec with MockitoSugar with ScalaCheckPropertyChecks {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new NewAccountDetailsFormProvider()
  val form: Form[NewAccountDetails] = formProvider()

  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  lazy val newAccountDetailsRoute: String =
    controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode).url
  val newAccountDetails: NewAccountDetails = NewAccountDetails("name", "123456", "11110000")
  val accountType:       WhatTypeOfAccount = WhatTypeOfAccount.Sole
  val userAnswers: UserAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      NewAccountDetailsPage.toString -> Json.toJsObject(newAccountDetails),
      WhatTypeOfAccountPage.toString -> Json.toJson(accountType)
    )
  )
  val userAnswersNoAccountDetails: UserAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      WhatTypeOfAccountPage.toString -> Json.toJson(accountType)
    )
  )

  "NewAccountDetails Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags())

      "must redirect to Pega if RedirectToPegaAction is enabled" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilderWithVerificationActions(
          config,
          userAnswers = Some(userAnswers),
          redirectToPegaAction = FakeRedirectToPegaAction(true)
        ).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(
            result
          ).get mustEqual "https://account.hmrc.gov.uk/child-benefit/make_a_claim/change-of-bank"
        }
      }

      "must return OK and the correct view for a GET" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[NewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(form.fill(newAccountDetails), NormalMode, accountType)(request, messages(application)).toString
          )
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)
        verifyClaimantBankAccountStub()
        val mockSessionRepository = mock[SessionRepository]
        val application = applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[NewAccountDetailsView]
          when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              form
                .bind(
                  Map(
                    "bacsError"             -> "",
                    "newSortCode"           -> newAccountDetails.newSortCode,
                    "newAccountHoldersName" -> newAccountDetails.newAccountHoldersName,
                    "newAccountNumber"      -> newAccountDetails.newAccountNumber
                  )
                ),
              NormalMode,
              accountType
            )(
              request,
              messages(application)
            ).toString
          )
        }
      }

      "must redirect to the next page when valid data is submitted" in {
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
              FakeRequest(POST, newAccountDetailsRoute)
                .withFormUrlEncodedBody(
                  ("newAccountHoldersName", newAccountDetails.newAccountHoldersName),
                  ("newSortCode", newAccountDetails.newSortCode),
                  ("newAccountNumber", newAccountDetails.newAccountNumber)
                )
                .withSession("authToken" -> "Bearer 123")
            )
          when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
          when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
        }
      }

      "must return a Bad Request and errors when valid data is submitted but Bacs fail" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)
        verifyClaimantBankAccountFailureStub(
          NOT_FOUND,
          "{\"status\": 404, \"description\": \"[priority2] - Sort Code Not Found\"}"
        )

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
              FakeRequest(POST, newAccountDetailsRoute)
                .withFormUrlEncodedBody(
                  ("newAccountHoldersName", newAccountDetails.newAccountHoldersName),
                  ("newSortCode", newAccountDetails.newSortCode),
                  ("newAccountNumber", newAccountDetails.newAccountNumber)
                )
                .withSession("authToken" -> "Bearer 123")
            )

          val expectedBoundForm = form.bind(
            Map(
              "bacsError"             -> "[priority2] - Sort Code Not Found",
              "newSortCode"           -> newAccountDetails.newSortCode,
              "newAccountHoldersName" -> newAccountDetails.newAccountHoldersName,
              "newAccountNumber"      -> newAccountDetails.newAccountNumber
            )
          )

          when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
          when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

          val result = route(application, request).value
          val view   = application.injector.instanceOf[NewAccountDetailsView]
          status(result) mustEqual BAD_REQUEST
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(expectedBoundForm, NormalMode, accountType)(request, messages(application)).toString
          )
        }
      }

      "must Redirect to Can not verify account Page when backend returns '[BARS locked] - The maximum number of retries reached when calling BAR' message" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)
        verifyClaimantBankAccountFailureStub(
          INTERNAL_SERVER_ERROR,
          lockedOutErrorResponse
        )

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
              FakeRequest(POST, newAccountDetailsRoute)
                .withFormUrlEncodedBody(
                  ("newAccountHoldersName", newAccountDetails.newAccountHoldersName),
                  ("newSortCode", newAccountDetails.newSortCode),
                  ("newAccountNumber", newAccountDetails.newAccountNumber)
                )
                .withSession("authToken" -> "Bearer 123")
            )

          when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(userAnswers)))
          when(mockSessionRepository.set(userAnswers)).thenReturn(Future.successful(true))

          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustEqual Some("/child-benefit/change-bank/cannot-verify-account")
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswersNoAccountDetails)).build()

        running(application) {
          val request =
            FakeRequest(POST, newAccountDetailsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))
              .withSession("authToken" -> "Bearer 123")

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[NewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(boundForm, NormalMode, accountType)(request, messages(application)).toString
          )
        }
      }

      "must return OK for a GET if no existing data is found" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswersNoAccountDetails)).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual 200
        }
      }

      "must redirect to service unavailable for a GET if no userAnswers is found at all" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.ServiceUnavailableController.onPageLoad.url
        }
      }

      "must return BAD_REQUEST for a POST if no existing data is found" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(userAnswersNoAccountDetails)).build()

        running(application) {
          val request =
            FakeRequest(POST, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must properly be redirected to Pega or Hicbc and Barlock validation in case " - {
        val scenarios = Table(
          ("RedirectToPega-VerifyHICBC-VerifyBARNotLocked", "StatusAndRedirectUrl"),
          (
            (true, true, false),
            (SEE_OTHER, Some("https://account.hmrc.gov.uk/child-benefit/make_a_claim/change-of-bank"))
          ),
          (
            (false, true, false),
            (SEE_OTHER, Some(controllers.cob.routes.BARSLockOutController.onPageLoad().url))
          ),
          (
            (false, false, true),
            (SEE_OTHER, Some(controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad().url))
          ),
          ((false, true, true), (OK, None))
        )

        forAll(scenarios) { (actions, statusAndRedirectUrl) =>
          val (redirectToPegaAction, hicbcAction, verificationBarAction) = actions
          val (resultStatus, redirectUrl)                                = statusAndRedirectUrl

          val application: Application = applicationBuilderWithVerificationActions(
            config,
            userAnswers = Some(userAnswers),
            verifyHICBCAction = FakeVerifyHICBCAction(hicbcAction),
            verifyBarNotLockedAction = FakeVerifyBarNotLockedAction(verificationBarAction),
            redirectToPegaAction = FakeRedirectToPegaAction(redirectToPegaAction)
          ).build()

          s"RedirectToPega: $redirectToPegaAction - Verify Bar Not Locked: $verificationBarAction - Verify Not HICBC: $hicbcAction \n" +
            s"\t\tshould return $resultStatus and redirect URL $redirectUrl" in {
            running(application) {
              mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
              userLoggedInIsChildBenefitUser(ninoUser)
              val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

              val result = route(application, request).value

              status(result) mustEqual resultStatus
              redirectLocation(result).fold(succeed)(_ must include(redirectUrl.get))
            }
          }
        }

      }
    }
    "when the change of bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorTemplate]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
              request,
              messages(application)
            ).toString
          )
        }
      }
    }
  }
}
