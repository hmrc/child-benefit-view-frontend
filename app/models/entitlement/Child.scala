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
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

final case class Child(
    name:                  FullName,
    dateOfBirth:           LocalDate,
    relationshipStartDate: LocalDate,
    relationshipEndDate:   Option[LocalDate],
    nino:                  Option[NationalInsuranceNumber],
    ninoSuffix:            Option[NinoSuffix],
    crnIndicator:          Option[Int]
) {
  def determineAgeLimit: Boolean = {
    val today = LocalDate.now()
    val ageLimit = today.minusYears(15).minusMonths(9)
    if (dateOfBirth.isBefore(ageLimit) || dateOfBirth.isEqual(ageLimit)) {
      true
    } else {
      false
    }
  }

  def crnIndicatorAsBoolean: Option[Boolean] = {
    crnIndicator.flatMap {
      case 0 => Some(false)
      case 1 => Some(true)
    }
  }
}

object Child {
  implicit val format: Format[Child] = Json.format[Child]
}
