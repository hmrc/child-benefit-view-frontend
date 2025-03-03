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
import models.pertaxAuth.PertaxAuthResponseModel
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStubs._
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData.ninoUser
import views.html.ErrorTemplate
import views.html.cob.AccountNotChangedView

class AccountNotChangedControllerSpec extends BaseAppSpec with ScalaCheckPropertyChecks {

  "AccountNotChanged Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and the correct view for a GET" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.AccountNotChangedController.onPageLoad().url)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[AccountNotChangedView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view()(request, messages(application)).toString
          )
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
            userAnswers = Some(emptyUserAnswers),
            verifyHICBCAction = FakeVerifyHICBCAction(hicbcAction),
            verifyBarNotLockedAction = FakeVerifyBarNotLockedAction(verificationBarAction),
            redirectToPegaAction = FakeRedirectToPegaAction(redirectToPegaAction)
          ).build()

          s"RedirectToPega: $redirectToPegaAction - Verify Bar Not Locked: $verificationBarAction - Verify Not HICBC: $hicbcAction \n" +
            s"\t\tshould return $resultStatus and redirect URL $redirectUrl" in {
            running(application) {
              val request = FakeRequest(GET, controllers.cob.routes.AccountNotChangedController.onPageLoad().url)
                .withSession("authToken" -> "Bearer 123")
              mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
              userLoggedInIsChildBenefitUser(ninoUser)
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
          val request = FakeRequest(GET, controllers.cob.routes.AccountNotChangedController.onPageLoad().url)
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
