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
import models.common.AdjustmentReasonCode
import models.entitlement.{AdjustmentInformation, ChildBenefitEntitlement, LastPaymentFinancialInfo}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty}
import services.PaymentHistoryPageVariant._
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.Stubs.{entitlementsAndPaymentHistoryFailureStub, entitlementsAndPaymentHistoryStub, userLoggedInChildBenefitUser}
import utils.TestData.{NinoUser, entitlementResult, NotFoundAccountError}
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

    "must return OK and render the correct view when entitlement ended, claimant is HICBIC and no payments in last 2 years" in {
      userLoggedInChildBenefitUser(NinoUser)
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
      userLoggedInChildBenefitUser(NinoUser)
      entitlementsAndPaymentHistoryFailureStub(NotFoundAccountError, status = 404)

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
    entitlementResult.copy(claimant =
      entitlementResult.claimant.copy(lastPaymentsInfo =
        Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

  val entitlementResultIsHIBICWithPaymentsInLastTwoYears: ChildBenefitEntitlement = entitlementResult.copy(claimant =
    entitlementResult.claimant.copy(adjustmentInformation =
      Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementResultIsHIBICWithoutPaymentsInLastTwoYears: ChildBenefitEntitlement = entitlementResult.copy(claimant =
    entitlementResult.claimant.copy(
      lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400)),
      adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
    )
  )

  val entitlementResultIsHIBICWithoutPaymentsInLastTwoYearsEndDateInPast: ChildBenefitEntitlement =
    entitlementResult.copy(claimant =
      entitlementResult.claimant.copy(
        awardEndDate = LocalDate.now().minusDays(100),
        lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400)),
        adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("28"), LocalDate.now.plusDays(10)))
      )
    )

  val entitlementEndedButReceivedPaymentsInLastTwoYears: ChildBenefitEntitlement =
    entitlementResult.copy(claimant = entitlementResult.claimant.copy(awardEndDate = LocalDate.now().minusDays(100)))

  val entitlementEndedButNoPaymentsInLastTwoYears: ChildBenefitEntitlement =
    entitlementResult.copy(claimant =
      entitlementResult.claimant.copy(
        awardEndDate = LocalDate.now().minusDays(100),
        lastPaymentsInfo = Seq(LastPaymentFinancialInfo(creditDate = LocalDate.now.minusYears(4), 400))
      )
    )

}
