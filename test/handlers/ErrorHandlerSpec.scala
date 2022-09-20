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

package handlers

import controllers.routes
import org.scalatest.EitherValues
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.{InternalServerError, SeeOther}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.BaseISpec
import views.html.ErrorTemplate

class ErrorHandlerSpec extends BaseISpec with EitherValues {
  "Error handler" - {
    "handling error must render redirect to service unavailable page for status 503" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")

        val errorHandler = application.injector.instanceOf[ErrorHandler]

        val result = errorHandler.handleError(Status.SERVICE_UNAVAILABLE, "test")

        result mustEqual SeeOther(routes.ServiceUnavailableController.onPageLoad.url)
      }
    }

    "handling error must render standard error page for statuses other than 503" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")

        val errorHandler = application.injector.instanceOf[ErrorHandler]
        val view         = application.injector.instanceOf[ErrorTemplate]

        val result = errorHandler.handleError(Status.BAD_REQUEST, "test")

        result mustEqual InternalServerError(
          view("global.error.InternalServerError500.title", "global.error.InternalServerError500.heading", "test")(
            request,
            messages(application, request)
          )
        )
      }
    }
  }
}
