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

package utils

import models.common.{AddressLine, AddressPostcode}
import models.entitlement.{AdjustmentInformation, AdjustmentReasonCode, Child, ChildBenefitEntitlement, Claimant, FullAddress, FullName, LastPaymentFinancialInfo}

import java.time.LocalDate

object TestData {

  val NinoUser =
    """
      |{
      |	"nino": "QQ123456A",
      |	"credentialRole": "User",
      |	"internalId": "Int-8612ba91-5581-411d-9d32-fb2de937a565"
      |}
      |""".stripMargin

  val entitlementResult = ChildBenefitEntitlement(
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
        AddressLine("AddressLine1"),
        AddressLine("AddressLine2"),
        Some(AddressLine("AddressLine3")),
        Some(AddressLine("AddressLine4")),
        Some(AddressLine("AddressLine5")),
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
}
