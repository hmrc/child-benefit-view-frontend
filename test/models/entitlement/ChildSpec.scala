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

package models.entitlement

import models.common.NationalInsuranceNumber
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

import java.time.LocalDate

class ChildSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  val childJson =
    """{
      | "ninoWithoutSuffix": "TE12345",
      | "dateOfBirth": "2021-01-01",
      | "name": "Test Name",
      | "relationshipStartDate": "2021-01-01",
      | "ninoSuffix": "ST",
      | "crnIndicator": 0
      |}
      |""".stripMargin

  val missingChildJson =
    """{
      | "dateOfBirth": "2021-01-01",
      | "name": "Test Name",
      | "relationshipStartDate": "2021-01-01"
      |}
      |""".stripMargin
  "Child" - {

    "must deserialise valid values" in {
      val expectedChild = Child(
        FullName("Test Name"),
        LocalDate.of(2021, 1, 1),
        LocalDate.of(2021, 1, 1),
        None,
        Some(NationalInsuranceNumber("TE12345")),
        Some(NinoSuffix("ST")),
        Some(0)
      )

      Json.parse(childJson).as[Child] mustEqual expectedChild
    }

    "must deserialise missing values" in {
      val expectedChild =
        Child(FullName("Test Name"), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 1), None, None, None, None)

      Json.parse(missingChildJson).as[Child] mustEqual expectedChild
    }

    "must correctly calculate age" in {
      val olderChild =
        Child(FullName("Test Name"), LocalDate.of(1995, 1, 1), LocalDate.of(2021, 1, 1), None, None, None, None)
      val youngerChild =
        Child(FullName("Test Name"), LocalDate.of(2023, 1, 1), LocalDate.of(2021, 1, 1), None, None, None, None)

      olderChild.determineAgeLimit mustEqual true
      youngerChild.determineAgeLimit mustEqual false
    }

    "must correctly convert crnIndicator" in {
      val noCrnChild =
        Child(FullName("Test Name"), LocalDate.of(1995, 1, 1), LocalDate.of(2021, 1, 1), None, None, None, Some(0))
      val crnChild =
        Child(FullName("Test Name"), LocalDate.of(2023, 1, 1), LocalDate.of(2021, 1, 1), None, None, None, Some(1))

      crnChild.crnIndicatorAsBoolean mustEqual Some(true)
      noCrnChild.crnIndicatorAsBoolean mustEqual Some(false)
    }

  }
}
