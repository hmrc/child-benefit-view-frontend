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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser
import views.html.ErrorTemplate
import views.html.cob.HICBCOptedOutPaymentsView

class HICBCOptedOutPaymentsControllerSpec extends BaseAppSpec {

  "HICBCOptedOutPayments Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and the correct view for a GET" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad().url)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[HICBCOptedOutPaymentsView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result).trim(),
            view()(request, messages(application)).toString.trim()
          )
        }
      }
    }

    "when the change-of-bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad().url)
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
