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

package utils

import models.changeofbank._
import models.common._
import models.entitlement._

import java.time.LocalDate

object TestData {

  val ninoUser: String =
    """
      |{
      |	"nino": "This",
      |	"affinityGroup": "Individual",
      |	"internalId": "Int-8612ba91-5581-411d-9d32-fb2de937a565"
      | "confidenceLevel": "200"
      |}
      |""".stripMargin

  def invalidJsonResponse: String =
    """
      | Invalid: This is not valid Json
      |""".stripMargin

  def genericCBError(status: Int, description: String): String =
    s"""
      |{
      |		"status": $status,
      |		"description": "$description"
      |	}
      |""".stripMargin

  val notFoundAccountError: String =
    """
      |{
      |		"status": 404,
      |		"description": "NOT_FOUND_CB_ACCOUNT - downstream service returned NOT_FOUND_IDENTIFIER, suggesting user does not have a child benefit account"
      |	}
      |""".stripMargin

  val lockedOutErrorDescription = "[BAR locked] - The maximum number of retries reached when calling BAR"
  val lockedOutErrorResponse: String =
    s"""
      |{
      |		"status": 500,
      |		"description": "$lockedOutErrorDescription"
      |	}
      |""".stripMargin

  val barsFailureErrorDescription = "[Priority] - A unit test BARS failure has occurred"
  val barsFailureErrorResponse: String =
    s"""
      |{
      |		"status": 404,
      |		"description": "$barsFailureErrorDescription"
      |	}
      |""".stripMargin

  val ftnaeNoChBAccountErrorResponse: String =
    """
      |{
      |		"status": 404,
      |		"description": "No ChB Account"
      |	}
      |""".stripMargin

  val ftnaeCannotFindYoungPersonErrorResponse: String =
    """
      |{
      |		"status": 404,
      |		"description": "Can not find young person"
      |	}
      |""".stripMargin

  val testEntitlement: ChildBenefitEntitlement = ChildBenefitEntitlement(
    Claimant(
      name = FullName("John Doe"),
      awardValue = 500.00,
      awardStartDate = LocalDate.now(),
      awardEndDate = LocalDate.now.plusYears(3),
      higherRateValue = 1000.00,
      standardRateValue = 50.00,
      lastPaymentsInfo = List(
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(2), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(3), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(4), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(5), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(6), 50.00)
      ),
      fullAddress = FullAddress(
        AddressLine("Addressline1"),
        AddressLine("Addressline2"),
        Some(AddressLine("Addressline3")),
        Some(AddressLine("Addressline4")),
        Some(AddressLine("Addressline5")),
        AddressPostcode("SS1 7JJ")
      ),
      adjustmentInformation = Some(AdjustmentInformation(AdjustmentReasonCode("12"), LocalDate.now().minusDays(4)))
    ),
    entitlementDate = LocalDate.now(),
    paidAmountForEldestOrOnlyChild = 25.10,
    paidAmountForEachAdditionalChild = 14.95,
    children = List(
      Child(
        FullName("Full Name"),
        dateOfBirth = LocalDate.of(2012, 1, 1),
        relationshipStartDate = LocalDate.of(2013, 1, 1),
        relationshipEndDate = Some(LocalDate.of(2016, 1, 1))
      )
    )
  )

  val testClaimantBankInformation: ClaimantBankInformation = ClaimantBankInformation(
    firstForename = FirstForename("John"),
    surname = Surname("Doe"),
    dateOfBirth = LocalDate.of(1955, 1, 26),
    activeChildBenefitClaim = true,
    financialDetails = ClaimantFinancialDetails(
      awardEndDate = LocalDate.now.plusYears(2),
      adjustmentReasonCode = None,
      adjustmentEndDate = None,
      bankAccountInformation = ClaimantBankAccountInformation(
        accountHolderName = Some(AccountHolderName("Mr J Doe")),
        sortCode = Some(SortCode("112233")),
        bankAccountNumber = Some(BankAccountNumber("12345678")),
        buildingSocietyRollNumber = None
      )
    )
  )
}
