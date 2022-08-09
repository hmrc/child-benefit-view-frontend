/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class AwardInformation(awardValue: Double,
                            startDate:  LocalDate,
                            endDate:    LocalDate)

object AwardInformation {
  implicit val format: Format[AwardInformation] = Json.format[AwardInformation]
}
