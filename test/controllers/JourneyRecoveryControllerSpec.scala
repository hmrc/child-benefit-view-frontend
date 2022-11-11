/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import utils.Stubs.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.TestData.NinoUser
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

class JourneyRecoveryControllerSpec extends BaseISpec {

  "JourneyRecovery Controller" - {

    "when a relative continue Url is supplied" - {

      "must return OK and the continue view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val continueUrl = RedirectUrl("/foo")
          val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(Some(continueUrl)).url)
            .withSession(("authToken", "Bearer 123"))

          val result = route(app, request).value

          val continueView = app.injector.instanceOf[JourneyRecoveryContinueView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            continueView(continueUrl.unsafeValue)(
              request,
              messages(application)
            ).toString
          )
        }
      }
    }

    "when an absolute continue Url is supplied" - {

      "must return OK and the start again view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val continueUrl = RedirectUrl("https://foo.com")
          val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(Some(continueUrl)).url)
            .withSession(("authToken", "Bearer 123"))

          val result = route(app, request).value

          val startAgainView = app.injector.instanceOf[JourneyRecoveryStartAgainView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            startAgainView()(request, messages(application)).toString
          )
        }
      }
    }

    "when no continue Url is supplied" - {

      "must return OK and the start again view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad().url)
            .withSession(("authToken", "Bearer 123"))

          val result = route(app, request).value

          val startAgainView = app.injector.instanceOf[JourneyRecoveryStartAgainView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            startAgainView()(request, messages(application)).toString
          )
        }
      }
    }
  }
}
