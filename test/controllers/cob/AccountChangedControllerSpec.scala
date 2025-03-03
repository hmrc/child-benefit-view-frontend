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
import models.cob.{NewAccountDetails, UpdateBankDetailsResponse}
import models.errors.ConnectorError
import models.pertaxAuth.PertaxAuthResponseModel
import models.requests.BaseDataRequest
import models.{CBEnvelope, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.cob.NewAccountDetailsPage
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.ChangeOfBankService
import stubs.AuthStubs._
import testconfig.TestConfig
import testconfig.TestConfig._
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData.ninoUser
import views.html.ErrorTemplate
import views.html.cob.AccountChangedView

import scala.concurrent.{ExecutionContext, Future}
import stubs.AuthStubs
import utils.TestData

class AccountChangedControllerSpec extends BaseAppSpec with MockitoSugar with ScalaCheckPropertyChecks {
  val mockSessionRepository = mock[SessionRepository]
  val mockCoBService        = mock[ChangeOfBankService]

  val successfulUpdateBankDetailsResponse = UpdateBankDetailsResponse("Success")

  val newAccountDetails         = NewAccountDetails("John Doe", "123456", "09876543")
  val accountDetailsUserAnswers = UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).get

  "AccountChanged Controller" - {
    "onPageLoad" - {
      "GIVEN the change of bank feature is enabled" - {
        val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))
        when(mockSessionRepository.get(userAnswersId)).thenReturn(Future.successful(Some(accountDetailsUserAnswers)))

        "WHEN both the Change of Bank Submission and Cache Clear are successful" - {
          "THEN should return OK Result and the expected view" in {
            when(
              mockCoBService.submitClaimantChangeOfBank(
                any[Option[NewAccountDetails]],
                any[BaseDataRequest[AnyContent]]
              )(any[ExecutionContext], any[HeaderCarrier])
            ).thenReturn(CBEnvelope(successfulUpdateBankDetailsResponse))
            when(mockCoBService.dropChangeOfBankCache()(any[ExecutionContext], any[HeaderCarrier]))
              .thenReturn(CBEnvelope(()))

            mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
            userLoggedInIsChildBenefitUser(ninoUser)

            val application = applicationBuilderWithVerificationActions(
              config,
              userAnswers = Some(UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).get)
            ).overrides(
              bind[ChangeOfBankService].toInstance(mockCoBService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(GET, controllers.cob.routes.AccountChangedController.onPageLoad().url)
                .withSession("authToken" -> "Bearer 123")

              val result = route(application, request).value

              val view = application.injector.instanceOf[AccountChangedView]

              status(result) mustEqual OK
              assertSameHtmlAfter(removeCsrfAndNonce)(
                contentAsString(result),
                view()(request, messages(application)).toString
              )
            }
          }

          "AND the following Verify Action return the respective result" - {
            val actionScenarios = Table(
              ("RedirectToPega", "VerifyBarNotLocked", "VerifyNotHICBC", "ResultName", "ExpectedResult", "ExpectedUrl"),
              (
                true,
                true,
                false,
                "SEE_OTHER",
                SEE_OTHER,
                Some("https://account.hmrc.gov.uk/child-benefit/make_a_claim/change-of-bank")
              ),
              (
                false,
                true,
                false,
                "SEE_OTHER",
                SEE_OTHER,
                Some(controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad().url)
              ),
              (
                false,
                false,
                true,
                "SEE_OTHER",
                SEE_OTHER,
                Some(controllers.cob.routes.BARSLockOutController.onPageLoad().url)
              ),
              (false, true, true, "OK", OK, None)
            )

            forAll(actionScenarios) {
              (redirectToPega, verifyBarNotLocked, verifyNotHICBC, resultName, expectedResult, expectedUrl) =>
                s"RedirectToPega: $redirectToPega - Verify Bar Not Locked: $verifyBarNotLocked - Verify Not HICBC: $verifyNotHICBC" - {
                  s"THEN should return $resultName and redirect URL $expectedUrl" in {
                    val application = applicationBuilderWithVerificationActions(
                      config,
                      userAnswers = Some(UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).get),
                      verifyBarNotLockedAction = FakeVerifyBarNotLockedAction(verifyBarNotLocked),
                      verifyHICBCAction = FakeVerifyHICBCAction(verifyNotHICBC),
                      redirectToPegaAction = FakeRedirectToPegaAction(redirectToPega)
                    ).overrides(
                      bind[ChangeOfBankService].toInstance(mockCoBService),
                      bind[SessionRepository].toInstance(mockSessionRepository)
                    ).build()

                    running(application) {
                      val request = FakeRequest(GET, controllers.cob.routes.AccountChangedController.onPageLoad().url)
                        .withSession("authToken" -> "Bearer 123")
                      mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
                      AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

                      val result = route(application, request).value

                      status(result) mustEqual expectedResult
                      redirectLocation(result).fold(
                        redirectLocation(result) mustEqual None
                      )(location => location must include(expectedUrl.get))
                    }
                  }
                }
            }
          }
        }

        "WHEN the Change of Bank Submission fails" - {
          "THEN should return SEE_OTHER Result" in {
            when(
              mockCoBService.submitClaimantChangeOfBank(
                any[Option[NewAccountDetails]],
                any[BaseDataRequest[AnyContent]]
              )(any[ExecutionContext], any[HeaderCarrier])
            ).thenReturn(CBEnvelope.fromError(ConnectorError(INTERNAL_SERVER_ERROR, "Unit Test Error")))

            val application = applicationBuilderWithVerificationActions(
              config,
              userAnswers = Some(UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).get)
            ).overrides(
              bind[ChangeOfBankService].toInstance(mockCoBService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(GET, controllers.cob.routes.AccountChangedController.onPageLoad().url)
                .withSession("authToken" -> "Bearer 123")
              mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
              AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
            }
          }
        }

        "WHEN the Cache Clear fails" - {
          "THEN should return SEE_OTHER Result" in {
            when(
              mockCoBService.submitClaimantChangeOfBank(
                any[Option[NewAccountDetails]],
                any[BaseDataRequest[AnyContent]]
              )(any[ExecutionContext], any[HeaderCarrier])
            ).thenReturn(CBEnvelope(successfulUpdateBankDetailsResponse))
            when(mockCoBService.dropChangeOfBankCache()(any[ExecutionContext], any[HeaderCarrier]))
              .thenReturn(CBEnvelope.fromError(ConnectorError(INTERNAL_SERVER_ERROR, "Unit Test Error")))

            val application = applicationBuilderWithVerificationActions(
              config,
              userAnswers = Some(UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).get)
            ).overrides(
              bind[ChangeOfBankService].toInstance(mockCoBService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(GET, controllers.cob.routes.AccountChangedController.onPageLoad().url)
                .withSession("authToken" -> "Bearer 123")

              mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
              AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
            }
          }
        }
      }

      "GIVEN the change of bank feature is disabled" - {
        val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

        "WHEN a call is made" - {
          "THEN should return Not Found result and the Error View" in {
            mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
            userLoggedInIsChildBenefitUser(ninoUser)

            val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

            running(application) {
              val request = FakeRequest(GET, controllers.cob.routes.AccountChangedController.onPageLoad().url)
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
  }
}
