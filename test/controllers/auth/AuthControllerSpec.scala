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
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import stubs.AuthStubs._
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.http.StringContextOps
import utils.TestData.ninoUser

import java.net.URLEncoder

class AuthControllerSpec extends BaseAppSpec {

  "AuthController" - {
    "on SignOut redirect to /gg/sign-out with continue to the feedback survey" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      val application = applicationBuilder()
        .configure(
          Map(
            "urls.signOut"                                -> "Blah blah blah",
            "microservice.services.feedback-frontend.url" -> "The exit survey"
          )
        )
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.AuthController.signOut().url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          s"Blah blah blah?continue=${URLEncoder.encode("The exit survey/feedback/CHIB", "utf-8")}"
        )
      }
    }

    "on SignOut without survey redirect to /gg/sign-out without continue to the feedback survey" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      val application = applicationBuilder()
        .configure(
          Map(
            "urls.signOut" -> "Blah blah blah"
          )
        )
        .build()

      val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey().url)
        .withSession(("authToken", "Bearer 123"))

      running(application) {
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          s"Blah blah blah?continue=${URLEncoder.encode(routes.SignedOutController.onPageLoad.absoluteURL()(request), "utf-8")}"
        )
      }
    }
  }
}
