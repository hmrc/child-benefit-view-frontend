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

package controllers.ftnae

import base.BaseAppSpec
import models.pertaxAuth.PertaxAuthResponseModel
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import views.html.ftnae.WhyYoungPersonNotListedView
import utils.TestData
import stubs.AuthStubs
import stubs.AuthStubs.mockPostPertaxAuth

class WhyYoungPersonNotListedControllerSpec extends BaseAppSpec {

  "WhyYoungPersonNotListed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.WhyYoungPersonNotListedController.onPageLoad().url)
          .withSession("authToken" -> "Bearer 123")
        mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
        AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhyYoungPersonNotListedView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view()(request, messages(application)).toString
        )
      }
    }
  }
}
