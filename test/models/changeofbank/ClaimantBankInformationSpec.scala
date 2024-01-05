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

package models.changeofbank

import base.BaseSpec
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class ClaimantBankInformationSpec extends BaseSpec {
  "ClaimantBankInformationSpec" - {
    "GIVEN a valid first forename, surname, date of birth, whether they have an active child benefit claim and financial details" - {
      "THEN the expected ClaimantBankInformationSpec is returned" in {
        forAll(
          arbitrary[FirstForename],
          arbitrary[Surname],
          arbitrary[LocalDate],
          arbitrary[Boolean],
          arbitrary[ClaimantFinancialDetails]
        ) { (firstForename, surname, dateOfBirth, hasClaim, financialDetails) =>
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
      forAll(
        arbitrary[FirstForename],
        arbitrary[Surname],
        arbitrary[LocalDate],
        arbitrary[Boolean],
        arbitrary[ClaimantFinancialDetails]
      ) { (firstForename, surname, dateOfBirth, hasClaim, financialDetails) =>
        val claimantBankInformation =
          ClaimantBankInformation(firstForename, surname, dateOfBirth, hasClaim, financialDetails)
        Json.toJson(claimantBankInformation)
      }
    }
  }
}
