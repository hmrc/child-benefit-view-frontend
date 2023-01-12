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

package controllers

import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Stubs.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.TestData.NinoUser
import models.viewmodels.govuk.SummaryListFluency
import testconfig.TestConfig
import testconfig.TestConfig._
import views.html.{CheckYourAnswersView, ErrorTemplate}

class CheckYourAnswersControllerSpec extends BaseISpec with SummaryListFluency {

  "Check Your Answers Controller" - {

    "when the new-claim feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(newClaim = true))

      "must return OK and the correct view for a GET" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers))
          .configure(
            "microservice.services.auth.port" -> wiremockPort
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
            .withSession(("authToken", "Bearer 123"))

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]
          val list = SummaryListViewModel(Seq.empty)

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(contentAsString(result), view(list)(request, messages(application)).toString)
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = None).configure().build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
            .withSession(("authToken", "Bearer 123"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    "when the new-claim feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(newClaim = false))

      "must return Not Found and the Error view" in {
        val application = applicationBuilder(config).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
          val view    = application.injector.instanceOf[ErrorTemplate]

          val result = route(application, request).value

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
              request,
              messages(application, request)
            ).toString
          )
        }
      }
    }
  }
}
