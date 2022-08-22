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

import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.AuthStub.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.TestData.NinoUser

class HomeControllerSpec extends BaseISpec {

  private val fakeRequest = FakeRequest("GET", "/").withSession(("authToken", "Bearer 123"))
  private val controller  = app.injector.instanceOf[HomeController]

  "render home" - {
    "return HTML and 200 status" in {
      userLoggedInChildBenefitUser(NinoUser)

      val result = controller.view()(fakeRequest)
      status(result) mustEqual Status.OK
      contentType(result) mustEqual Some("text/html")
      charset(result) mustEqual Some("utf-8")
      contentAsString(result) must include("Apply for Child Benefit")
    }
  }

}
