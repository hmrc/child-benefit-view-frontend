/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Name(nameSequenceNumber: Int,
                nameType:           Int,
                titleType:          Int,
                requestedName:      String,
                nameStartDate:      LocalDate,
                nameEndDate:        LocalDate,
                otherTitle:         String,
                honours:            String,
                firstForename:      String,
                secondForename:     String,
                surname:            String)

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}
