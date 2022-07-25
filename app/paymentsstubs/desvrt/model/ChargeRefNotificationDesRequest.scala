/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt.model

import play.api.libs.json._

//TODO We don't know what this looks like yet
case class ChargeRefNotificationDesRequest(
    taxType:         String,
    chargeRefNumber: String,
    amountPaid:      BigDecimal)

object ChargeRefNotificationDesRequest {
  implicit val format: OFormat[ChargeRefNotificationDesRequest] = Json.format[ChargeRefNotificationDesRequest]
}

