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

package controllers.auth

import base.BaseAppSpec
import config.FrontendAppConfig
import play.api.http.Status.SEE_OTHER
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, redirectLocation, status}
import stubs.AuthStubs._
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import utils.TestData.ninoUser

class AuthControllerSpec extends BaseAppSpec {

  val application = applicationBuilder().build()
  lazy val controller = application.injector.instanceOf[AuthController]
  lazy val appConfig  = application.injector.instanceOf[FrontendAppConfig]

  "AuthController" - {
    "redirect to /gg/sign-out with continue to the feedback survey" in {
      userLoggedInIsChildBenefitUser(ninoUser)

      val request = FakeRequest(GET, routes.AuthController.signOut().url)
        .withSession(("authToken", "Bearer 123"))

      val result = controller.signOut()(request)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(
        appConfig.signOutUrl + s"?continue=${SafeRedirectUrl(appConfig.exitSurveyUrl).encodedUrl}"
      )

    }
  }
}
