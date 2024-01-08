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
