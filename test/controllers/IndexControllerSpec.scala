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
import utils.TestData.NinoUser
import views.html.IndexView

class IndexControllerSpec extends BaseISpec {

  "Index Controller" - {

    lazy val indexController = app.injector.instanceOf[IndexController]
    lazy val view            = app.injector.instanceOf[IndexView]

    "must return OK and the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)
        .withSession(("authToken", "Bearer 123"))
      val result = indexController.onPageLoad(request)

      status(result) mustEqual 200
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }
}
