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

package controllers.auth

import config.FrontendAppConfig
import play.api.http.Status.SEE_OTHER
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import utils.BaseISpec
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser

class AuthControllerSpec extends BaseISpec {

  lazy val controller = app.injector.instanceOf[AuthController]
  lazy val appConfig  = app.injector.instanceOf[FrontendAppConfig]

  "AuthController" - {
    "redirect to /gg/sign-out with continue to the feedback survey" in {
      userLoggedInChildBenefitUser(NinoUser)

      val request = FakeRequest(GET, routes.AuthController.signOut.url)
        .withSession(("authToken", "Bearer 123"))

      val result = controller.signOut()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(
        appConfig.signOutUrl + s"?continue=${SafeRedirectUrl(appConfig.exitSurveyUrl).encodedUrl}"
      )

    }
  }
}
