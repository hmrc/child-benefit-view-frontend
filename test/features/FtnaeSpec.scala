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

package features

import base.BaseAppSpec
import models.NormalMode
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.AuthStubs
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData
import views.html.ftnae.FtnaeDisabledView

class FtnaeSpec extends BaseAppSpec {
  "A controller covered by the Ftnae Feature flag" - {
    "when the FTNAE feature is disabled" - {
      "Should return Not Found with the disabledView" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .configure(
            Map(
              "feature-flags.ftnae.enabled" -> false
            )
          )
          .build()

        val schoolOrCollegeRoute = controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(NormalMode).url

        running(application) {
          val request = FakeRequest(GET, schoolOrCollegeRoute)
            .withSession("authToken" -> "Bearer 123")
          AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FtnaeDisabledView]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(hideWrapperBanner = true)(request, messages(application)).toString
          )
        }
      }
    }
  }
}
