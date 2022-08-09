/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class AdjustmentType(adjustmentAmount:     Double,
                          adjustmentStartDate:  LocalDate,
                          adjustmentEndDate:    LocalDate,
                          adjustmentReasonCode: String)

object AdjustmentType {
  implicit val format: Format[AdjustmentType] = Json.format[AdjustmentType]
}
