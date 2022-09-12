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

import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.AuthStub.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.NonceUtils.removeNonce
import utils.TestData.NinoUser
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends BaseISpec with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .configure(
          "microservice.services.auth.port" -> wiremockPort
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(contentAsString(result), view(list)(request, messages(application)).toString)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
