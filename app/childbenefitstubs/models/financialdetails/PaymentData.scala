/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class PaymentData(amount:                Double,
                       dateFrom:              LocalDate,
                       dateTo:                LocalDate,
                       chargeRef:             String,
                       expectedCreditingDate: LocalDate,
                       returnIndicator:       Int)

object PaymentData {
  implicit val format: Format[PaymentData] = Json.format[PaymentData]
}
