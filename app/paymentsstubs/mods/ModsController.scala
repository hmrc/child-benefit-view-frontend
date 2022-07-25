/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.mods

import paymentsstubs.mods.model.ModsPaymentCallBackRequest
import play.api.Logger
import play.api.libs.json.Json.toJson

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class ModsController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  val logger: Logger = Logger(this.getClass)

  def paymentCallback: Action[ModsPaymentCallBackRequest] = Action(parse.json[ModsPaymentCallBackRequest]) { implicit request =>
    logger.info("Received MODS payment callback POST request:\n" + s"""${toJson[ModsPaymentCallBackRequest](request.body)}""")
    request.body.chargeReference match {
      case "modsnotfound"    => NotFound
      case "modsservererror" => InternalServerError
      case _                 => Ok
    }
  }

  def receiveModsCallback(modsReference: String): Action[AnyContent] = Action {
    modsReference match {
      case "modsnotfound"    => NotFound
      case "modsservererror" => InternalServerError
      case _                 => Ok
    }
  }

}
