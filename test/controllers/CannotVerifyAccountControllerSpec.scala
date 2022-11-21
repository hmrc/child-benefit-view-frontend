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

import base.CobSpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.HtmlMatcherUtils.removeNonce
import views.html.cob.CannotVerifyAccountView

class CannotVerifyAccountControllerSpec extends CobSpecBase {

  "CannotVerifyAccount Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.cob.routes.CannotVerifyAccountController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotVerifyAccountView]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(contentAsString(result), view()(request, messages(application)).toString)
      }
    }
  }
}