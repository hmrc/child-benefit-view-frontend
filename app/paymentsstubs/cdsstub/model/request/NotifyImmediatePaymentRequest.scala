/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.cdsstub.model.request

import play.api.libs.json.{Json, OFormat}

final case class CDSRequestPost(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest)

final case class NotifyImmediatePaymentRequest(
    requestCommon: RequestCommon,
    requestDetail: RequestDetail)

final case class RequestCommon(receiptDate: String, acknowledgementReference: String, regime: String, originatingSystem: String) {
  require(regime == "CDS")
  require(originatingSystem == "OPS")
}

final case class RequestDetail(paymentReference: String, amountPaid: String, unitType: String, declarationID: String) {
  require(unitType == "GBP")
}

object CDSRequestPost {

  implicit val requestCommonFormat: OFormat[RequestCommon] = Json.format[RequestCommon]
  implicit val requestDetailFormat: OFormat[RequestDetail] = Json.format[RequestDetail]
  implicit val validNotifyImmediatePaymentRequestFormat: OFormat[NotifyImmediatePaymentRequest] = Json.format[NotifyImmediatePaymentRequest]
  implicit val cDSRequestPostFormat: OFormat[CDSRequestPost] = Json.format[CDSRequestPost]

}
