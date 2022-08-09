/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Residency(residencySequenceNumber: Int,
                     dateLeavingUK:           LocalDate,
                     dateReturningUK:         LocalDate,
                     residencyStatusFlag:     Int)

object Residency {
  implicit val format: OFormat[Residency] = Json.format[Residency]
}
