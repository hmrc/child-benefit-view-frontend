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

package controllers

import base.BaseAppSpec
import controllers.PaymentHistoryControllerSpec._
import models.common.AdjustmentReasonCode
import models.entitlement.{AdjustmentInformation, ChildBenefitEntitlement, LastPaymentFinancialInfo}
import play.api.http.Status.{NOT_FOUND, OK, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import services.PaymentHistoryPageVariant._
import utils.HtmlMatcherUtils.removeNonce
import stubs.AuthStubs._
import stubs.ChildBenefitServiceStubs._
import utils.TestData.{ninoUser, notFoundAccountError, testEntitlement}
import views.html.paymenthistory.{NoPaymentHistory, PaymentHistory}

import java.time.LocalDate

class PaymentHistoryControllerSpec extends BaseAppSpec {

  "Payment history controller" - {
    "must return OK and render the correct view when entitlement contains payment with payments in last 2 years" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      entitlementsAndPaymentHistoryStub(testEntitlement)

      val application = applicationBuilder().build()
      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[PaymentHistory]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(testEntitlement, InPaymentWithPaymentsInLastTwoYears)(request, messages(application, request)).toString
        )
      }
    }

    "must return OK and render the correct view when entitlement contains payment without payments in last 2 years" in {
      userLoggedInIsChildBenefitUser(ninoUser)
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
      userLoggedInIsChildBenefitUser(ninoUser)
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
      userLoggedInIsChildBenefitUser(ninoUser)
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
      userLoggedInIsChildBenefitUser(ninoUser)
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
      userLoggedInIsChildBenefitUser(ninoUser)
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

    "must return OK and render the correct view when entitlement ended, claimant is HICBIC and no payments in last 2 years" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      entitlementsAndPaymentHistoryStub(entitlementResultIsHIBICWithoutPaymentsInLastTwoYearsEndDateInPast)

      val application = applicationBuilder().build()
      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoPaymentHistory]

        status(result) mustEqual OK

        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(
            entitlementResultIsHIBICWithoutPaymentsInLastTwoYearsEndDateInPast,
            HICBCWithoutPaymentsInLastTwoYearsAndEndDateInPast
          )(
            request,
            messages(application, request)
          ).toString
        )
      }
    }

    "must return 404 and render the no account found view when services return no found account" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      entitlementsAndPaymentHistoryFailureStub(NOT_FOUND, notFoundAccountError)

      val application = applicationBuilder().build()
      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.PaymentHistoryController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
}

object PaymentHistoryControllerSpec {

  val entitlementResultWithoutPaymentsInLastTwoYears: ChildBenefitEntitlement =
    testEntitlement.copy(claimant =
      testEntitlement.claimant.copy(lastPaymentsInfo =
        Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

  val entitlementResultIsHIBICWithPaymentsInLastTwoYears: ChildBenefitEntitlement = testEntitlement.copy(claimant =
    testEntitlement.claimant.copy(adjustmentInformation =
      Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementResultIsHIBICWithoutPaymentsInLastTwoYears: ChildBenefitEntitlement = testEntitlement.copy(claimant =
    testEntitlement.claimant.copy(
      lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400)),
      adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementResultIsHIBICWithoutPaymentsInLastTwoYearsEndDateInPast: ChildBenefitEntitlement =
    testEntitlement.copy(claimant =
      testEntitlement.claimant.copy(
        awardEndDate = LocalDate.now().minusDays(100),
        lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400)),
        adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
      )
    )

  val entitlementEndedButReceivedPaymentsInLastTwoYears: ChildBenefitEntitlement =
    testEntitlement.copy(claimant = testEntitlement.claimant.copy(awardEndDate = LocalDate.now().minusDays(100)))

  val entitlementEndedButNoPaymentsInLastTwoYears: ChildBenefitEntitlement =
    testEntitlement.copy(claimant =
      testEntitlement.claimant.copy(
        awardEndDate = LocalDate.now().minusDays(100),
        lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

}
