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

import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser
import views.html.NoAccountFoundView

class NoAccountFoundControllerSpec extends BaseISpec {
  "NoAccountFoundController" - {
    "must return OK and the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoAccountFoundController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))
        val result = route(application, request).value

        val view = application.injector.instanceOf[NoAccountFoundView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view()(request, messages(application)).toString
        )

      }
    }

    "must return UNAUTHORISED and the correct view for a GET" in {

      userLoggedInChildBenefitUser(NinoUser)
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoAccountFoundController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value must startWith(application.configuration.get[String]("urls.login"))

      }

    }
  }
}
