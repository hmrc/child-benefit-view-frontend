/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import paymentsstubs.cdsstub.model.request.CDSRequestPost
import paymentsstubs.cdsstub.model.response._

import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ISO_DATE
import scala.collection.mutable
import play.api.mvc._
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton()
class ChildBenefitFinancialDetails @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  val validFinancialDetails: String =
    """
      {
        "debtIndicator": 1,
        "awardInformation": [
         {
            "awardValue": 0.00,
           "startDate": "2010-12-31",
           "endDate": "2028-12-30"
     } ],
        "rateInformation": {
          "higherRateValue": 99999999999.99,
         "standardRateValue": 0.0,
          "guardianAllowanceRateValue": 123456.78
      } }
      """

  def paymentDetails(identifier: String): Action[AnyContent] = Action { implicit request =>

    identifier match {
      case "AB654321" => Ok(validFinancialDetails).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")
    }

  }
}
