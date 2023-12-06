package models.changeofbank

import base.BaseSpec
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ClaimantBankInformationSpec extends BaseSpec {
  "ClaimantBankInformationSpec" -  {
    "GIVEN a valid first forename, surname, date of birth, whether they have an active child benefit claim and financial details" - {
      "THEN the expected ClaimantBankInformationSpec is returned" in {
        forAll(arbitrary[FirstForename], arbitrary[Surname], arbitrary[LocalDate], arbitrary[Boolean], arbitrary[ClaimantFinancialDetails]) {
          (firstForename, surname, dateOfBirth, hasClaim, financialDetails) =>
          val result = ClaimantBankInformation(firstForename, surname, dateOfBirth, hasClaim, financialDetails)

          result.firstForename mustBe firstForename
          result.surname mustBe surname
          result.dateOfBirth mustBe dateOfBirth
          result.activeChildBenefitClaim mustBe hasClaim
          result.financialDetails mustBe financialDetails
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[FirstForename], arbitrary[Surname], arbitrary[LocalDate], arbitrary[Boolean], arbitrary[ClaimantFinancialDetails]) {
        (firstForename, surname, dateOfBirth, hasClaim, financialDetails) =>
        val claimantBankInformation = ClaimantBankInformation(firstForename, surname, dateOfBirth, hasClaim, financialDetails)
        Json.toJson(claimantBankInformation)
      }
    }
  }
}
