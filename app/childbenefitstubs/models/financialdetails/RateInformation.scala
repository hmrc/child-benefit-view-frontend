/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

case class RateInformation(higherRateValue:            Double,
                           standardRateValue:          Double,
                           guardianAllowanceRateValue: Double)

object RateInformation {
  implicit val format: Format[RateInformation] = Json.format[RateInformation]
}
