package models.audit

import base.BaseSpec
import models.entitlement.Child
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

private class ClaimantEntitlementDetailsSpec extends BaseSpec {
  "ClaimantEntitlementDetails" - {
    "GIVEN a valid name, address, amount, entitlement start, entitlement end and a sequence of children" - {
      "THEN the expected ClaimantEntitlementDetails is returned" in {
        forAll(
          generateName,
          generateAddressLine,
          arbitrary[BigDecimal],
          arbitrary[LocalDate],
          arbitrary[LocalDate],
          arbitrary[Seq[Child]]
        ) { (name, address, amount, startDate, endDate, children) =>
          val result = ClaimantEntitlementDetails(name, address, amount, startDate.toString, endDate.toString, children)

          result.name mustBe name
          result.address mustBe address
          result.amount mustBe amount
          result.start mustBe startDate.toString
          result.end mustBe endDate.toString
          result.children mustBe children
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        generateName,
        generateAddressLine,
        arbitrary[BigDecimal],
        arbitrary[LocalDate],
        arbitrary[LocalDate],
        arbitrary[Seq[Child]]
      ) { (name, address, amount, startDate, endDate, children) =>
        val claimantEntitlementDetails =
          ClaimantEntitlementDetails(name, address, amount, startDate.toString, endDate.toString, children)
        Json.toJson(claimantEntitlementDetails)
      }

    }
  }
}
