/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Address(addressSequenceNumber:    Int,
                   addressSource:            Int,
                   countryCode:              Int,
                   addressType:              Int,
                   addressStatus:            Int,
                   addressStartDate:         LocalDate,
                   addressEndDate:           LocalDate,
                   addressLastConfirmedDate: LocalDate,
                   vpaMail:                  Int,
                   deliveryInfo:             String,
                   pafReference:             String,
                   addressLine1:             String,
                   addressLine2:             String,
                   addressLine3:             String,
                   addressLine4:             String,
                   addressLine5:             String,
                   addressPostcode:          String)

object Address {
  implicit val format: OFormat[Address] = Json.format[Address]
}
