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

import base.BaseAppSpec
import controllers.cob.{routes => cobRoutes}
import controllers.ftnae.{routes => ftnaeroutes}
import controllers.routes
import models.errors._
import org.mockito.Mockito.verify
import org.scalatest.EitherValues
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import utils.handlers.ErrorHandlerSpec.resultUrl
import views.html.{ErrorTemplate, NotFoundView}

class ErrorHandlerSpec extends BaseAppSpec with EitherValues {
  "Error handler" - {
    implicit val auditServiceMock = mock[AuditService]
    implicit val fakeRequest      = FakeRequest(GET, "/")
    implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global

    implicit val messages: Messages = mock[Messages]
    val messagesApiMock   = mock[MessagesApi]
    val notFoundViewMock  = mock[NotFoundView]
    val errorTemplateMock = mock[ErrorTemplate]

    val sut = new ErrorHandler(messagesApiMock, notFoundViewMock, errorTemplateMock)

    "notFoundTemplate" - {
      "GIVEN a notFoundView is provided to the ErrorHandler" - {
        "THEN this is view return as the notFoundTemplate" in {
          val result = sut.notFoundTemplate

          result mustBe notFoundViewMock()
        }
      }
    }

    "standardErrorTemplate" - {
      "GIVEN a ErrorTemplate is provided to the ErrorHandler" - {
        "THEN this is view return as the standardErrorTemplate" in {
          val result = sut.standardErrorTemplate("Unit Test Title", "Unit Test Heading", "Unit Test Message")

          result mustBe errorTemplateMock("Unit Test Title", "Unit Test Heading", "Unit Test Message")
        }
      }
    }

    "handleError" - {
      val noAccountMessage = "NOT_FOUND_CB_ACCOUNT"

      "Connector Errors" - {
        s"GIVEN a Not Found ($noAccountMessage) ConnectorError" - {
          val error = ConnectorError(NOT_FOUND, noAccountMessage)

          "THEN the result is a Redirect to No Account Found" in {
            val result = sut.handleError(error, None)

            result.header.status mustEqual SEE_OTHER
            resultUrl(result) mustEqual routes.NoAccountFoundController.onPageLoad.url
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

        "GIVEN an Internal Server Error ConnectorError" - {
          val error = ConnectorError(INTERNAL_SERVER_ERROR, "")

          "THE result is a Redirect to Service Unavailable" in {
            val result = sut.handleError(error, None)

            result.header.status mustEqual SEE_OTHER
            resultUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
          }
        }

        "GIVEN any other ConnectorError" - {
          val error = ConnectorError(0, "")

          "THE result is a Redirect to Service Unavailable" in {
            val result = sut.handleError(error, None)

            result.header.status mustEqual SEE_OTHER
            resultUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
          }
        }
      }

      "GIVEN a ClaimantIsLockedOutOfChangeOfBank error" - {
        val error = ClaimantIsLockedOutOfChangeOfBank(0, "")

        "THE the result is a Redirect to BARS Lock Out" in {
          val result = sut.handleError(error, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual cobRoutes.BARSLockOutController.onPageLoad().url
        }
      }

      "GIVEN a PaymentHistoryValidationError error" - {
        val error = PaymentHistoryValidationError(0, "")

        "THEN the result is a Redirect to Service Unavailable" in {
          val result = sut.handleError(error, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
        }
      }

      "GIVEN a FtnaeNoCHBAccountError" - {
        "THEN the result is a Redirect to No Account Found" in {
          val result = sut.handleError(FtnaeNoCHBAccountError, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual routes.NoAccountFoundController.onPageLoad.url
        }
      }

      "GIVEN a FtnaeCannotFindYoungPersonError" - {
        "THEN the result is a Redirect to Cannot Find Young Person" in {
          val result = sut.handleError(FtnaeCannotFindYoungPersonError, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual ftnaeroutes.CannotFindYoungPersonController.onPageLoad.url
        }
      }

      "GIVEN a FtnaeChildUserAnswersNotRetrieved" - {
        "THEN the result is a Redirect to Service Unavailable" in {
          val result = sut.handleError(FtnaeChildUserAnswersNotRetrieved, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
        }
      }

      "GIVEN any other Type of CBError" - {
        "THEN the result is a Redirect to Service Unavailable" in {
          val result = sut.handleError(UnitTestError, None)

          result.header.status mustEqual SEE_OTHER
          resultUrl(result) mustEqual routes.ServiceUnavailableController.onPageLoad.url
        }
      }
    }

    "ErrorHandler companion object" - {
      "logMessage" - {
        val expectedMessage     = "Unit Test testing log message"
        val expectedCode        = 404
        val expectedAuditOrigin = "Unit Test Origin"

        "GIVEN a message value is provided" - {
          val containMessageTestCases = Table(
            ("withCode", "withAuditOrigin"),
            (true, false),
            (true, true),
            (false, false),
            (false, true)
          )

          forAll(containMessageTestCases) { (withCode, withAuditOrigin) =>
            s"THEN the message value appears in the returned message ${withOrWithout(withCode)} code and ${withOrWithout(withAuditOrigin)} audit origin" in {
              val code        = if (withCode) Some(expectedCode) else None
              val auditOrigin = if (withAuditOrigin) Some(expectedAuditOrigin) else None
              val result      = ErrorHandler.logMessage(expectedMessage, code, auditOrigin)

              result.contains(expectedMessage) mustBe true
            }
          }
        }

        val trueFalseTestCases = Table("value", true, false)

        forAll(trueFalseTestCases) { withCode =>
          s"GIVEN that a code ${isOrIsNot(withCode)} provided" - {
            forAll(trueFalseTestCases) { withAuditOrigin =>
              s"THEN the code appears in the returned message ${withOrWithout(withAuditOrigin)} an audit origin" in {
                val code        = if (withCode) Some(expectedCode) else None
                val auditOrigin = if (withAuditOrigin) Some(expectedAuditOrigin) else None
                val result      = ErrorHandler.logMessage(expectedMessage, code, auditOrigin)

                result.contains(s"$expectedCode") mustBe withCode
              }
            }
          }
        }

        forAll(trueFalseTestCases) { withAuditOrigin =>
          s"GIVEN that an audit origin ${isOrIsNot(withAuditOrigin)} provided" - {
            forAll(trueFalseTestCases) { withCode =>
              s"THEN the audit origin appears in the returned message ${withOrWithout(withCode)} a code" in {
                val code        = if (withCode) Some(expectedCode) else None
                val auditOrigin = if (withAuditOrigin) Some(expectedAuditOrigin) else None
                val result      = ErrorHandler.logMessage(expectedMessage, code, auditOrigin)

                result.contains(expectedAuditOrigin) mustBe withAuditOrigin
              }
            }
          }
        }
      }
    }
  }
}

final case object UnitTestError extends CBError {
  override val statusCode = NOT_IMPLEMENTED
  override val message    = "Unit Test error"
}

object ErrorHandlerSpec {
  val baseChBUrl = "/child-benefit"
  def resultUrl(result: Result): String =
    s"${result.header.headers.getOrElse("Location", "[location not found]")}"

}
