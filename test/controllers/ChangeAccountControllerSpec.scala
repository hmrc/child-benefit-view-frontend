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

import controllers.ChangeAccountControllerSpec.{claimantBankInformationWithBuildingSocietyRollNumber, claimantBankInformationWithEndDateInPast, claimantBankInformationWithEndDateToday, claimantBankInformationWithHICBC}
import models.changeofbank.{BuildingSocietyRollNumber, ClaimantBankInformation}
import models.common.AdjustmentReasonCode
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.Stubs._
import utils.TestData.{LockedOutErrorResponse, NinoUser, NotFoundAccountError, claimantBankInformation}
import views.html.cob.ChangeAccountView

import java.time.LocalDate

class ChangeAccountControllerSpec extends BaseISpec {

  "ChangeAccount Controller" - {

    "must return OK and render the correct view for ChB claimant who is in payment and has a standard bank account type" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformation)
      verifyClaimantBankInfoStub()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeAccountView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view()(request, messages(application)).toString
        )
      }
    }

    "must return OK and render the correct view for ChB claimant who is in payment and has a non-standard bank account type" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformationWithBuildingSocietyRollNumber)
      verifyClaimantBankInfoStub()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeAccountView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view()(request, messages(application)).toString
        )
      }
    }

    "must return SEE_OTHER and render the correct view for ChB claimant who is currently locked out of the service due to 3 x BARS failures in 24-hours" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformation)
      verifyClaimantBankInfoFailureStub(result = LockedOutErrorResponse)

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ("/child-benefit/change-bank/locked-out")
      }
    }

    "must return SEE_OTHER and render the correct view for ChB claimant who is opted out of payments due to HICBC" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformationWithHICBC)
      verifyClaimantBankInfoStub()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/child-benefit/change-bank/service-not-available/opted-out-payments"

      }
    }

    "must return SEE_OTHER and render the correct view for a terminated ChB claim with an end date in the past" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformationWithEndDateInPast)
      verifyClaimantBankInfoStub()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/child-benefit/no-account-found"

      }
    }

    "must return SEE_OTHER and render the correct view for a terminated ChB claim with an end date is day of request" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoStub(claimantBankInformationWithEndDateToday)
      verifyClaimantBankInfoStub()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/child-benefit/no-account-found"
      }
    }

    "must return SEE_OTHER and render the correct view for No ChB account found" in {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      userLoggedInChildBenefitUser(NinoUser)
      changeOfBankUserInfoFailureStub(result = NotFoundAccountError)
      verifyClaimantBankInfoStub()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/child-benefit/no-account-found"

      }
    }
  }
}

object ChangeAccountControllerSpec {

  val claimantBankInformationWithBuildingSocietyRollNumber: ClaimantBankInformation =
    claimantBankInformation.copy(financialDetails =
      claimantBankInformation.financialDetails.copy(bankAccountInformation =
        claimantBankInformation.financialDetails.bankAccountInformation
          .copy(buildingSocietyRollNumber = Some(BuildingSocietyRollNumber("1234")))
      )
    )

  val claimantBankInformationWithHICBC: ClaimantBankInformation = claimantBankInformation.copy(financialDetails =
    claimantBankInformation.financialDetails.copy(
      adjustmentReasonCode = Some(AdjustmentReasonCode("28")),
      adjustmentEndDate = Some(LocalDate.now.plusYears(2))
    )
  )

  val claimantBankInformationWithEndDateInPast: ClaimantBankInformation =
    claimantBankInformation.copy(
      activeChildBenefitClaim = false,
      financialDetails = claimantBankInformation.financialDetails.copy(awardEndDate = LocalDate.now.minusYears(1))
    )

  val claimantBankInformationWithEndDateToday: ClaimantBankInformation = claimantBankInformation.copy(financialDetails =
    claimantBankInformation.financialDetails.copy(awardEndDate = LocalDate.now)
  )

}
