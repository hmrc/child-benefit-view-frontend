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

import controllers.PaymentHistoryControllerSpec._
import models.entitlement.{AdjustmentInformation, AdjustmentReasonCode, ChildBenefitEntitlement, PaymentFinancialInfo}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import services.PaymentHistoryPageVariant._
import utils.BaseISpec
import utils.NonceUtils.removeNonce
import utils.Stubs.{entitlementsAndPaymentHistoryFailureStub, entitlementsAndPaymentHistoryStub, userLoggedInChildBenefitUser}
import utils.TestData.{NinoUser, entitlementResult, entitlementServiceNotFoundAccountError}
import views.html.paymenthistory.{NoPaymentHistory, PaymentHistory}

import java.time.LocalDate

class PaymentHistoryControllerSpec extends BaseISpec {

  "Payment history controller" - {
    "must return OK and render the correct view when entitlement contains payment with payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementResult)

      running(app) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(app, request).value

        val view = app.injector.instanceOf[PaymentHistory]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementResult, InPaymentWithPaymentsInLastTwoYears)(request, messages(app, request)).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement contains payment without payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementResultWithoutPaymentsInLastTwoYears)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoPaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementResultWithoutPaymentsInLastTwoYears, InPaymentWithoutPaymentsInLastTwoYears)(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement is HICBC with payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementResultIsHIBICWithPaymentsInLastTwoYears)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementResultIsHIBICWithPaymentsInLastTwoYears, HICBCWithPaymentsInLastTwoYears)(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement is HICBC without payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementResultIsHIBICWithoutPaymentsInLastTwoYears)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoPaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementResultIsHIBICWithoutPaymentsInLastTwoYears, HICBCWithoutPaymentsInLastTwoYears)(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement ended but received payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementEndedButReceivedPaymentsInLastTwoYears)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementEndedButReceivedPaymentsInLastTwoYears, EntitlementEndedButReceivedPaymentsInLastTwoYears)(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement ended and no payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryStub(entitlementEndedButNoPaymentsInLastTwoYears)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoPaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlementEndedButNoPaymentsInLastTwoYears, EntitlementEndedButNoPaymentsInLastTwoYears)(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return 404 and render the no account found view when services return no found account" in {
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryFailureStub(entitlementServiceNotFoundAccountError, status = 404)

      val application = applicationBuilder().build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
  "getStatus" - {
    "must return relative audit status" - {
      "Entitlement end date in future, No Adjustment code 28 - active account" in {

        val endDate = LocalDate.now.plusYears(1)
        val payments = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val result = PaymentHistoryController.getStatus(endDate, payments, None, None)

        result mustBe "Active - Payments"

      }
      "Adjustment code 28 (HICBC) with an end date = today [OR in the past], At least one payment issued in last 2 years - active account" in {

        val endDate = LocalDate.now
        val payments = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val reason = Some("28")
        val adjustmentEndDate = Some(LocalDate.now)

        val result = PaymentHistoryController.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - Payments"

      }

      "Adjustment code 28 (HICBC) with an end date = today OR in the past, No payment issued in last 2 years" in {

        val endDate = LocalDate.now
        val payments = Seq(
          PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400),
          PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(3), 500))
        val reason = Some("28")
        val adjustmentEndDate = Some(LocalDate.now)

        val result = PaymentHistoryController.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - No payments"
      }
      "when claimant has opt out due to HICBC - one payment at least in the previous two years" in {

        val endDate = LocalDate.now
        val reason = Some("28")
        val payments = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = PaymentHistoryController.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "HICBC - Payments"
      }
      "when claimant has opt out due to HICBC - no payment in the last two years" in {

        val endDate = LocalDate.now
        val reason = Some("28")
        val payments = Seq.empty
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = PaymentHistoryController.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "HICBC - No payments"
      }
      "when claimant has payment suspended - active account" in {
        val endDate = LocalDate.now.plusYears(2)
        val payments = Seq.empty
        val reason = Some("28")
        val adjustmentEndDate = Some(LocalDate.now.plusYears(1))

        val result = PaymentHistoryController.getStatus(endDate, payments, reason, adjustmentEndDate)

        result mustBe "Active - No payments"
      }
      "when claimant has no payment - inactive account" in {

        val endDate = LocalDate.now.minusYears(1)
        val payments = Seq.empty

        val result = PaymentHistoryController.getStatus(endDate, payments, None, None)

        result mustBe "Inactive - No payments"
      }
      "when claimant has at least a single payment within the previous two years - inactive account." in {

        val endDate = LocalDate.now.minusYears(1)
        val payments = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(1), 400))

        val result = PaymentHistoryController.getStatus(endDate, payments, None, None)

        result mustBe "Inactive - Payments"
      }
    }
  }
}

object PaymentHistoryControllerSpec {

  val entitlementResultWithoutPaymentsInLastTwoYears: ChildBenefitEntitlement =
    entitlementResult.copy(claimant =
      entitlementResult.claimant.copy(lastPaymentsInfo =
        Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

  val entitlementResultIsHIBICWithPaymentsInLastTwoYears: ChildBenefitEntitlement = entitlementResult.copy(claimant =
    entitlementResult.claimant.copy(adjustmentInformation =
      Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementResultIsHIBICWithoutPaymentsInLastTwoYears: ChildBenefitEntitlement = entitlementResult.copy(claimant =
    entitlementResult.claimant.copy(
      lastPaymentsInfo = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400)),
      adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementEndedButReceivedPaymentsInLastTwoYears: ChildBenefitEntitlement =
    entitlementResult.copy(claimant = entitlementResult.claimant.copy(awardEndDate = LocalDate.now().minusDays(100)))

  val entitlementEndedButNoPaymentsInLastTwoYears: ChildBenefitEntitlement =
    entitlementResult.copy(claimant =
      entitlementResult.claimant.copy(
        awardEndDate = LocalDate.now().minusDays(100),
        lastPaymentsInfo = Seq(PaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

}
