/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.email

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

case class EmailRequest(
    to:         Seq[String],
    templateId: String,
    parameters: Map[String, String],
    force:      Boolean             = false)

object EmailRequest {
  implicit val format: OFormat[EmailRequest] = Json.format[EmailRequest]
}

@Singleton
class EmailController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {
  val sendEmail: Action[EmailRequest] = Action(parse.json[EmailRequest]) { implicit request =>
    if (request.body.to.exists(_.contains("simulate.failure"))) throw new RuntimeException("Boom - Simulating failure")
    else if (request.body.to.exists(_.contains("simulate.not_found"))) NotFound("Boom - Simulating Not Found")
    else Ok("Email Sent")
  }
}
