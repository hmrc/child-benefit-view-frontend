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

package utils.handlers

import controllers.cob.{routes => cobRoutes}
import controllers.routes
import models.errors.{ClaimantIsLockedOutOfChangeOfBank, ConnectorError, PaymentHistoryValidationError}
import org.mockito.Mockito.verify
import org.mockito.MockitoSugar.mock
import org.scalatest.EitherValues
import play.api.i18n.MessagesApi
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import utils.BaseISpec
import utils.handlers.ErrorHandlerSpec.expectedUrl
import views.html.{ErrorTemplate, NotFoundView}

class ErrorHandlerSpec extends BaseISpec with EitherValues {
  "Error handler" - {
    implicit val auditServiceMock = mock[AuditService]
    implicit val fakeRequest      = FakeRequest(GET, "/")
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    val messagesApiMock   = mock[MessagesApi]
    val notFoundViewMock  = mock[NotFoundView]
    val errorTemplateMock = mock[ErrorTemplate]

    val sut = new ErrorHandler(messagesApiMock, notFoundViewMock, errorTemplateMock)

    val noAccountMessage = "NOT_FOUND_CB_ACCOUNT"
    s"GIVEN a Not Found ($noAccountMessage) ConnectorError" - {
      val error = ConnectorError(NOT_FOUND, noAccountMessage)

      "THEN the result is a Redirect to No Account Found" in {
        val result = sut.handleError(error, None)

        result.header.status mustEqual SEE_OTHER
        expectedUrl(result) mustEqual routes.NoAccountFoundController.onPageLoad.url
      }

      val pOEAuditOrigin = "proofOfEntitlement"
      s"AND the auditOrigin is $pOEAuditOrigin" - {
        "THEN auditProofOfEntitlement is called" in {
          sut.handleError(error, Some(pOEAuditOrigin))

          verify(auditServiceMock).auditProofOfEntitlement(
            "Unknown",
            "No Accounts Found",
            _,
            None
          )
        }
      }

      val pDAuditOrigin = "paymentDetails"
      s"AND the auditOrigin is $pDAuditOrigin" - {
        "THEN auditPaymentDetails is called" in {
          sut.handleError(error, Some(pDAuditOrigin))

          verify(auditServiceMock).auditPaymentDetails(
            "nino",
            "No Accounts Found",
            _,
            None
          )
        }
      }
    }

    "GIVEN an ConnectorError with an Internal Server Error status code" - {
      val error = ConnectorError(INTERNAL_SERVER_ERROR, "")

      "THE result is a Redirect to Service Unavailable" in {
        val result = sut.handleError(error, None)

        result.header.status mustEqual SEE_OTHER
        expectedUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
      }
    }

    "GIVEN any other ConnectorError" - {
      val error = ConnectorError(0, "")

      "THE result is a Redirect to Service Unavailable" in {
        val result = sut.handleError(error, None)

        result.header.status mustEqual SEE_OTHER
        expectedUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
      }
    }

    "GIVEN a ClaimantIsLockedOutOfChangeOfBank error" - {
      val error = ClaimantIsLockedOutOfChangeOfBank(0, "")

      "THE result is a Redirect to BARS Lock Out" in {
        val result = sut.handleError(error, None)

        result.header.status mustEqual SEE_OTHER
        expectedUrl(result) mustEqual cobRoutes.BARSLockOutController.onPageLoad().url
      }
    }

    "GIVEN a PaymentHistoryValidationError error" - {
      val error = PaymentHistoryValidationError(0, "")

      "THE result is a Redirect to Service Unavailable" in {
        val result = sut.handleError(error, None)

        result.header.status mustEqual SEE_OTHER
        expectedUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
      }
    }
  }
}

object ErrorHandlerSpec {
  val baseChBUrl = "/child-benefit"
  def expectedUrl(result: Result): String =
    s"${result.header.headers.getOrElse("Location", "[location not found]")}"

}
