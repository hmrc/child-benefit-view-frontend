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
import models.pertaxAuth.PertaxAuthResponseModel
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStubs._
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.HtmlMatcherUtils.removeNonceAndMenuRight
import utils.TestData.ninoUser
import views.html.ErrorTemplate
import views.html.cob.CannotVerifyAccountView

class CannotVerifyAccountControllerSpec extends BaseAppSpec {

  "CannotVerifyAccount Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and the correct view for a GET" in {
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        userLoggedInIsChildBenefitUser(ninoUser)

        val application =
          applicationBuilderWithVerificationActions(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.CannotVerifyAccountController.onPageLoad().url)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[CannotVerifyAccountView]

          status(result) mustEqual OK

          assertSameHtmlAfter(removeNonceAndMenuRight)(
            contentAsString(result),
            view()(request, messages(application)).toString
          )
        }
      }
    }

    "when the change of bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        userLoggedInIsChildBenefitUser(ninoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.CannotVerifyAccountController.onPageLoad().url)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorTemplate]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeNonceAndMenuRight)(
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
