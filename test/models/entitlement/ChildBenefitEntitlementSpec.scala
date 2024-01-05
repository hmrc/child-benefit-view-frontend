/*
 * Copyright 2024 HM Revenue & Customs
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

package models.entitlement

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ChildBenefitEntitlementSpec extends BaseSpec {
  "ChildBenefitEntitlement" - {
    "GIVEN a valid claimant, entitlement date, first payment, additional payment and a list of children" - {
      "THEN the expected ChildBenefitEntitlement is returned" in {
        forAll(
          arbitrary[Claimant],
          arbitrary[LocalDate],
          arbitrary[BigDecimal],
          arbitrary[BigDecimal],
          arbitrary[List[Child]]
        ) { (claimant, entitlementDate, firstPayment, additionalPayments, children) =>
          val result = ChildBenefitEntitlement(claimant, entitlementDate, firstPayment, additionalPayments, children)

          result.claimant mustBe claimant
          result.entitlementDate mustBe entitlementDate
          result.paidAmountForEldestOrOnlyChild mustBe firstPayment
          result.paidAmountForEachAdditionalChild mustBe additionalPayments
          result.children mustBe children
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[Claimant],
        arbitrary[LocalDate],
        arbitrary[BigDecimal],
        arbitrary[BigDecimal],
        arbitrary[List[Child]]
      ) { (claimant, entitlementDate, firstPayment, additionalPayments, children) =>
        val childBenefitEntitlement =
          ChildBenefitEntitlement(claimant, entitlementDate, firstPayment, additionalPayments, children)
        Json.toJson(childBenefitEntitlement)
      }
    }
  }
}
