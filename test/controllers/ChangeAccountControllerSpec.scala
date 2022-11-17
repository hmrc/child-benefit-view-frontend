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
import models.cob.AccountDetails
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.HtmlMatcherUtils.removeNonce
import views.html.cob.ChangeAccountView

class ChangeAccountControllerSpec extends CobSpecBase {

  "ChangeAccount Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeAccountView]

        val name = "Liz Jones"
        val details = AccountDetails("Lizbeth Jones", "12-34-56", "123456789")

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(contentAsString(result), view(name, Some(details))(request, messages(application)).toString)
      }
    }
  }
}
