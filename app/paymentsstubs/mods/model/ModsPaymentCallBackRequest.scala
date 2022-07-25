/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.mods.model

import play.api.libs.json._

case class ModsPaymentCallBackRequest(
    chargeReference:    String,
    amendmentReference: Option[Int]
)

object ModsPaymentCallBackRequest {
  implicit val format: OFormat[ModsPaymentCallBackRequest] = Json.format[ModsPaymentCallBackRequest]
}
