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

package controllers

import base.BaseAppSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito.{when, verify}
import repositories.SessionRepository
import play.api.inject.bind
import scala.concurrent.Future
import models.UserAnswers
import stubs.AuthStubs
import utils.TestData
import org.mockito.ArgumentMatchers.anyString

class KeepAliveControllerSpec extends BaseAppSpec {
  "Should refresh the userAnswers" in {
    AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

    val mockSessionRepo = mock[SessionRepository]
    when(mockSessionRepo.keepAlive(anyString)).thenReturn(Future.successful(true))
    val userAnswers = UserAnswers("TEST_ID")

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(bind[SessionRepository].toInstance(mockSessionRepo))
      .build()

    running(application) {
      val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)
        .withSession(("authToken", "Bearer 123"))
      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockSessionRepo).keepAlive("TEST_ID")
    }
  }

  "Should return Ok when there are no user answers" in {
    AuthStubs.userLoggedInIsChildBenefitUser(TestData.ninoUser)

    val application = applicationBuilder(userAnswers = None).build()

    running(application) {
      val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)
        .withSession(("authToken", "Bearer 123"))
      val result = route(application, request).value

      status(result) mustEqual OK
    }
  }
}
