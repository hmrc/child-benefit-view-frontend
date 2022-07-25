/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.cdsstub.controllers

import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ISO_DATE
import javax.inject.{Inject, Singleton}
import paymentsstubs.cdsstub.model.request.CDSRequestPost
import paymentsstubs.cdsstub.model.response._
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.collection.mutable

@Singleton()
class ValidCDS @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {
  def validate(paymentReference: Option[String]): Action[AnyContent] = Action { implicit request =>
    if (!request.headers.hasHeader("Authorization")) Unauthorized("No auth header was sent...")
    else paymentReference match {
      case Some("CDSI191234567890") => Ok(validResponse(isPaymentReferenceActive = true)).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      case Some("XCDSU201234567")   => Ok(validResponse(isPaymentReferenceActive = true)).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      case Some("CDSI190987654321") => Ok(validResponse(isPaymentReferenceActive = false)).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      case Some("XCDSU207654321")   => Ok(validResponse(isPaymentReferenceActive = false)).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      case None                     => BadRequest(noReference).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      case _                        => NotFound(notFoundResponse).withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
    }
  }

  private def noReference: JsValue =
    toJson[cashDepositPayUpErrorResponse](
      new cashDepositPayUpErrorResponse(
        status  = 400, error = "Bad Request", message = "Invalid request, payment reference or declaration id is mandatory"))

  private def validResponse(isPaymentReferenceActive: Boolean) =
    toJson[ResponseCds](new ResponseCds(
      GetCashDepositSubscriptionDetailsResponse(
        ResponseCommon(),
        ResponseDetail(
          paymentReference           = "1234567890",
          declarationID              = "0987654321",
          paymentReferenceDate       = now().format(ISO_DATE),
          paymentReferenceCancelDate = now().format(ISO_DATE),
          isPaymentReferenceActive   = isPaymentReferenceActive))))

  private def notFoundResponse: JsValue =
    toJson[cashDepositPayUpErrorResponse](
      new cashDepositPayUpErrorResponse(
        error   = "Not Found", message = "No subscription is found for a given payment reference and/or declaration Id", status = 404))

  val confirm: Action[CDSRequestPost] = Action(parse.json[CDSRequestPost]) { implicit request =>
    if (!request.headers.hasHeader("Authorization")) Unauthorized("No auth header was sent...")
    else {
      val cdsReference = request.body.notifyImmediatePaymentRequest.requestDetail.paymentReference
      cdsReference match {
        case "CDSI191234567890"               => Ok.withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
        case "XCDSU201234567"                 => Ok.withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
        case ref if ref.contains("queuetest") => retrySimulator(ref)
        case _                                => BadRequest.withHeaders("x-correlation-id" -> "TEST_CORRELATION_ID")
      }
    }
  }

  private val cdsNotification = mutable.Map[String, Int]()

  private def retrySimulator(reference: String): Result = {
    val mostRight = reference.takeRight(1)
    if (mostRight.matches("[-+]?\\d+(\\.\\d+)?") && (1 to 9).contains(mostRight.toInt)) {

      val counterOption: Option[Int] = cdsNotification.get(reference)
      val newCount: Int = counterOption match {
        case Some(counter) => counter + 1
        case None          => 1
      }
      if (newCount <= mostRight.toInt) {
        if (reference.contains("404")) {
          Logger(this.getClass).debug(s"oh no, cds notification response: [ not found ] $newCount")
          NotFound(s"oh no, cds notification response: [ not found ] $newCount")
        } else if (reference.contains("400")) {
          Logger(this.getClass).debug(s"oh no, cds notification response: [ bad request ] $newCount")
          BadRequest(s"oh no, cds notification response: [ bad request ] $newCount")
        } else {
          Logger(this.getClass).debug(s"oh no, cds notification response: [ server error ] $newCount")
          cdsNotification(reference) = newCount
          InternalServerError(s"oh no, cds notification response: [ server error ] $newCount")
        }
      } else {
        cdsNotification.remove(reference)
        Ok(s"Finished retrying, count $newCount")
      }
    } else {
      Ok("Did not retry")
    }
  }

  val resetCdsNotifications: Action[AnyContent] = Action {
    cdsNotification.clear()
    Ok("CDS notifications reset")
  }
}
