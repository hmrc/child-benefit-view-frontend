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

package features

import base.BaseAppSpec
import play.api.test.Helpers._
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import stubs.AuthStubs._
import utils.TestData.ninoUser

import views.html.templates.OldLayoutProvider
import uk.gov.hmrc.govukfrontend.views.html.components.GovukButton
import views.html.components.ParagraphBody
import views.html.cob.AccountNotChangedView
import views.html.components.Heading
import play.api.test.FakeRequest
import org.scalatest.prop.TableDrivenPropertyChecks

class WrapperSpec extends BaseAppSpec with TableDrivenPropertyChecks {

  "When the wrapper is disabled the old fallback layout is used" in {
    userLoggedInIsChildBenefitUser(ninoUser)
    val config =
      TestConfig().withFeatureFlags(featureFlags(changeOfBank = true)) + ("features.sca-wrapper-enabled" -> false)

    val application = applicationBuilderWithVerificationActions(config, userAnswers = Some(emptyUserAnswers))
      .build()

    running(application) {
      val request = FakeRequest(GET, controllers.cob.routes.AccountNotChangedController.onPageLoad().url)
        .withSession("authToken" -> "Bearer 123")

      val expectedViewWithOldLayout = new AccountNotChangedView(
        layout = application.injector.instanceOf[OldLayoutProvider],
        govukButton = application.injector.instanceOf[GovukButton],
        heading = application.injector.instanceOf[Heading],
        para = application.injector.instanceOf[ParagraphBody]
      )

      val result = route(application, request).value

      status(result) mustEqual OK

      assertSameHtmlAfter(removeCsrfAndNonce)(
        contentAsString(result),
        expectedViewWithOldLayout()(request, messages(application)).toString
      )
    }
  }
}
