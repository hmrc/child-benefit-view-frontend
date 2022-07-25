/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.cdsstub.model.response

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json._

case class cashDepositPayUpErrorResponse(
    timestamp: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    status:    Int,
    error:     String,
    message:   String)

object cashDepositPayUpErrorResponse {
  implicit val errorResponseFormat: OFormat[cashDepositPayUpErrorResponse] = Json.format[cashDepositPayUpErrorResponse]
}
case class ResponseCommon(status: String = "OK", processingDate: String = "2018-09-24T11:01:01Z")

case class ResponseDetail(
    declarationID:              String,
    paymentReference:           String,
    paymentReferenceDate:       String  = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    isPaymentReferenceActive:   Boolean,
    paymentReferenceCancelDate: String  = LocalDate.now().format(DateTimeFormatter.ISO_DATE))

case class ResponseCds(getCashDepositSubscriptionDetailsResponse: GetCashDepositSubscriptionDetailsResponse)
case class GetCashDepositSubscriptionDetailsResponse(
    responseCommon: ResponseCommon,
    responseDetail: ResponseDetail)

object ResponseCds {
  implicit val responseCommonFormat: OFormat[ResponseCommon] = Json.format[ResponseCommon]
  implicit val responseDetailFormat: OFormat[ResponseDetail] = Json.format[ResponseDetail]
  implicit val getCashDepositSubscriptionDetailsResponseFormat: OFormat[GetCashDepositSubscriptionDetailsResponse] = Json.format[GetCashDepositSubscriptionDetailsResponse]
  implicit val ResponseCdsFormat: OFormat[ResponseCds] = Json.format[ResponseCds]
}
