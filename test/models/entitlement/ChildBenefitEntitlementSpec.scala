package models.entitlement

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ChildBenefitEntitlementSpec extends BaseSpec {
  "ChildBenefitEntitlement" - {
    "GIVEN a valid claimant, entitlement date, first payment, additional payment and a list of children" - {
      "THEN the expected ChildBenefitEntitlement is returned" in {
        forAll(arbitrary[Claimant], arbitrary[LocalDate], arbitrary[BigDecimal], arbitrary[BigDecimal], arbitrary[List[Child]]) {
          (claimant, entitlementDate, firstPayment, additionalPayments, children) =>
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
      forAll(arbitrary[Claimant], arbitrary[LocalDate], arbitrary[BigDecimal], arbitrary[BigDecimal], arbitrary[List[Child]]) {
        (claimant, entitlementDate, firstPayment, additionalPayments, children) =>
        val childBenefitEntitlement = ChildBenefitEntitlement(claimant, entitlementDate, firstPayment, additionalPayments, children)
        Json.toJson(childBenefitEntitlement)
      }
    }
  }
}
