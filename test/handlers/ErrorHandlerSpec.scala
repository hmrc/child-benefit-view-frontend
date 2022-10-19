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
import models.errors.ConnectorError
import org.mockito.MockitoSugar.mock
import org.scalatest.EitherValues
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.SeeOther
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseISpec

import scala.concurrent.ExecutionContext

class ErrorHandlerSpec extends BaseISpec with EitherValues {
  "Error handler" - {
    "handling error must render redirect to service unavailable page for status 503" in {
      val application = applicationBuilder().build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
        implicit val auditor: AuditService                        = mock[AuditService]
        implicit val ec:      ExecutionContext                    = scala.concurrent.ExecutionContext.Implicits.global
        implicit val hc:      HeaderCarrier                       = HeaderCarrier()

        val errorHandler = application.injector.instanceOf[ErrorHandler]

        val result = errorHandler.handleError(ConnectorError(Status.SERVICE_UNAVAILABLE, "test"))

        result mustEqual SeeOther(routes.ServiceUnavailableController.onPageLoad.url)
      }
    }

  }
}
