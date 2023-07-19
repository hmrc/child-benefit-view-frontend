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

package controllers.cob

import forms.cob.WhatTypeOfAccountFormProvider
import models.cob.WhatTypeOfAccount
import models.{CBEnvelope, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import play.api.test.{CSRFTokenHelper, FakeRequest}
import repositories.SessionRepository
import testconfig.TestConfig
import testconfig.TestConfig._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.Stubs.{userLoggedInChildBenefitUser, verifyClaimantBankAccount}
import utils.TestData.NinoUser
import views.html.cob.WhatTypeOfAccountView
import pages.cob.WhatTypeOfAccountPage
import models.cob.WhatTypeOfAccount.{SoleAccount, JointAccountSharedWithSomeone}
import org.mockito.MockitoSugar.when
import org.mockito.Mockito.reset
import play.api.inject.bind

import scala.concurrent.{ExecutionContext, Future}

class WhatTypeOfAccountControllerSpec extends BaseISpec with MockitoSugar {

  implicit val mockExecutionContext = mock[ExecutionContext]
  implicit val mockHeaderCarrier    = mock[HeaderCarrier]

  val mockSessionRepository = mock[SessionRepository]

  def onwardRoute = Call("GET", "/foo")

  lazy val whatTypeOfAccountRoute: String =
    controllers.cob.routes.WhatTypeOfAccountController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(userAnswersId)

  val formProvider = new WhatTypeOfAccountFormProvider()
  val form: Form[WhatTypeOfAccount] = formProvider()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset[Object](
      mockSessionRepository,
      mockExecutionContext,
      mockHeaderCarrier
    )
  }

  "WhatTypeOfAccount Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must respond OK and the correct view for GET" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers))
          .build()

        running(application) {
          val request = FakeRequest(GET, whatTypeOfAccountRoute)
            .withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[WhatTypeOfAccountView]

          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(form, NormalMode)(request, messages(application)).toString
          )
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        userLoggedInChildBenefitUser(NinoUser)

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(WhatTypeOfAccountPage, JointAccountSharedWithSomeone)
          .success
          .value

        val application = applicationBuilder(config, userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, whatTypeOfAccountRoute)
            .withSession("authToken" -> "Bearer 123")

          when(mockSessionRepository.get(userAnswersId))
            .thenReturn(Future.successful(Some(userAnswers)))

          val view = application.injector.instanceOf[WhatTypeOfAccountView]

          val result = route(application, request).value

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              form.fill(JointAccountSharedWithSomeone),
              NormalMode
            )(request, messages(application)).toString
          )
        }
      }

    }

  }

}
