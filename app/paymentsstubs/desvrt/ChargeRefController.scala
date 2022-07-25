/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt

import javax.inject.{Inject, Singleton}
import paymentsstubs.desvrt.model.ChargeRefNotificationDesRequest
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.collection.mutable

@Singleton
class ChargeRefController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  private val chargeRefNotification = mutable.Map[String, Int]()

  val reset: Action[AnyContent] = Action {
    chargeRefNotification.clear()
    Ok
  }

  val cardNotification: Action[ChargeRefNotificationDesRequest] = Action(parse.json[ChargeRefNotificationDesRequest]) { implicit request =>
    import paymentsstubs.desvrt.model.ExtendedString._
    val mostRight = request.body.chargeRefNumber.takeRight(1)

    Logger(this.getClass).debug(s"""far right was $mostRight""")
    Logger(this.getClass).debug(
      s"""
         |{
         |  "taxType": "${request.body.taxType}",
         |  "chargeRefNumber": "${request.body.chargeRefNumber}",
         |  "amountPaid": ${request.body.amountPaid}
         |}
         |""".stripMargin)

    if (mostRight.isNumber && (1 to 9).contains(mostRight.toInt)) {

      val counterOption: Option[Int] = chargeRefNotification.get(request.body.chargeRefNumber)
      val newCount: Int = counterOption match {
        case Some(counter) => counter + 1
        case None          => 1
      }
      if (newCount <= mostRight.toInt) {
        if (request.body.chargeRefNumber.contains("404"))
          NotFound(s"oh no $newCount")
        else if (request.body.chargeRefNumber.contains("409"))
          Conflict(s"oh no $newCount")
        else if (request.body.chargeRefNumber.contains("400"))
          BadRequest(s"oh no $newCount")
        else {
          chargeRefNotification(request.body.chargeRefNumber) = newCount
          InternalServerError(s"oh no $newCount")
        }
      } else {
        chargeRefNotification.remove(request.body.chargeRefNumber)
        Ok(s"finished retrying, count $newCount")
      }
    } else {
      Ok("did not retry")
    }
  }
}
