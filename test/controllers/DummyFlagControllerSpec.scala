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

import org.scalatest.EitherValues
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import views.html.{DummyFlagView, ErrorTemplate}

class DummyFlagControllerSpec extends BaseISpec with EitherValues {
  "Dummy flag controller" - {
    "must render the correct view when flag is enabled" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.DummyFlagController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DummyFlagView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view()(request, messages(application, request)).toString
        )
      }
    }

    "must fail with 404 when flag is disabled" in {
      val application = applicationBuilder(config = Map("feature-flags.dummy-flag" -> false)).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.DummyFlagController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ErrorTemplate]

        status(result) mustEqual NOT_FOUND
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
            request,
            messages(application, request)
          ).toString
        )
      }
    }
  }
}
