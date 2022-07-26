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

package controllers.cob

import controllers.cob
import controllers.cob.ChangeAccountControllerSpec.{claimantBankInformationWithBuildingSocietyRollNumber, claimantBankInformationWithEndDateInPast, claimantBankInformationWithEndDateToday, claimantBankInformationWithHICBC}
import models.changeofbank._
import models.common.AdjustmentReasonCode
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.BaseISpec
import utils.HtmlMatcherUtils.{removeCsrfAndNonce, removeNonce}
import utils.Stubs._
import utils.TestData.{LockedOutErrorResponse, NinoUser, NotFoundAccountError, claimantBankInformation}
import views.html.ErrorTemplate
import views.html.cob.ChangeAccountView

import java.time.LocalDate

class ChangeAccountControllerSpec extends BaseISpec {

  "ChangeAccount Controller" - {

    val accountInfo = ClaimantBankAccountInformation(
      accountHolderName = Some(AccountHolderName("Mr J Doe")),
      sortCode = Some(SortCode("112233")),
      bankAccountNumber = Some(BankAccountNumber("12345678")),
      buildingSocietyRollNumber = None
    )

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and render the correct view for ChB claimant who is in payment and has a standard bank account type" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformation)
        verifyClaimantBankInfoStub()

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChangeAccountView]

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            view("John Doe", accountInfo)(request, messages(application)).toString
          )
        }
      }

      "must return OK and render the correct view for ChB claimant who is in payment and has a non-standard bank account type" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformationWithBuildingSocietyRollNumber)
        verifyClaimantBankInfoStub()

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChangeAccountView]

          val accountInfoWithRollNumber =
            accountInfo.copy(buildingSocietyRollNumber = Some(BuildingSocietyRollNumber("1234987650")))

          status(result) mustEqual OK
          assertSameHtmlAfter(removeNonce)(
            contentAsString(result),
            view("John Doe", accountInfoWithRollNumber)(request, messages(application)).toString
          )
        }
      }

      //Todo: This should be corrected in SB-1054
      "must return SEE_OTHER and render the correct view for ChB claimant who is currently locked out of the service due to 3 x BARS failures in 24-hours" ignore {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformation)
        verifyClaimantBankInfoFailureStub(result = LockedOutErrorResponse)

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual ("/child-benefit/change-bank/locked-out")
        }
      }

      "must return SEE_OTHER and render the correct view for ChB claimant who is opted out of payments due to HICBC" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformationWithHICBC)
        verifyClaimantBankInfoStub()

        running(application) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/child-benefit/change-bank/service-not-available/opted-out-payments"

        }
      }

      "must return SEE_OTHER and render the correct view for a terminated ChB claim with an end date in the past" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformationWithEndDateInPast)
        verifyClaimantBankInfoStub()

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/child-benefit/no-account-found"

        }
      }

      "must return SEE_OTHER and render the correct view for a terminated ChB claim with an end date is day of request" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoStub(claimantBankInformationWithEndDateToday)
        verifyClaimantBankInfoStub()

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/child-benefit/no-account-found"
        }
      }

      "must return SEE_OTHER and render the correct view for No ChB account found" in {
        val application: Application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        userLoggedInChildBenefitUser(NinoUser)
        changeOfBankUserInfoFailureStub(result = NotFoundAccountError)
        verifyClaimantBankInfoStub()

        running(application) {

          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, cob.routes.ChangeAccountController.onPageLoad().url)
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/child-benefit/no-account-found"
        }
      }
    }

    "when the change of bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.cob.routes.ChangeAccountController.onPageLoad().url)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorTemplate]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
              request,
              messages(application)
            ).toString
          )
        }
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
