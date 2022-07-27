/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

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

  val AB654321: String =
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

  val YY123499D: String =
    """
       {
  "debtIndicator": 1,
  "bankData": {
    "sortCode": "11-22-33",
    "bankAccountNumber": "987654321123456789",
    "accountHolderName": "MR JOHN DOE O'MALLEY & MRS O'MALLEY",
    "validFromDate": "1981-01-01"
  },
  "awardInformation": [
    {
      "awardValue": 0.00,
      "startDate": "2010-12-31",
      "endDate": "2018-12-30"
}, {
      "awardValue": 45.00,
      "startDate": "2018-12-31",
      "endDate": "2028-12-30"
} ],
  "rateInformation": {
    "higherRateValue": 99999999999.99,
    "standardRateValue": 0.0,
    "guardianAllowanceRateValue": 123456.78
  },
  "paymentData": [
    {
      "amount": -111.11,
      "dateFrom": "2010-12-31",
      "dateTo": "2011-01-30",
      "chargeRef": "A-131234/35",
      "expectedCreditingDate": "2010-02-03",
      "returnIndicator": 0
}, {
      "amount": 222.22,
      "dateFrom": "2010-12-31",
      "dateTo": "2011-01-30",
      "chargeRef": "A-131234/35",
      "expectedCreditingDate": "2010-02-03",
      "returnIndicator": 0
} ],
  "adjustmentType": [
    {
      "adjustmentAmount": 666666.66,
      "adjustmentStartDate": "2012-07-07",
      "adjustmentEndDate": "2013-02-28",
      "adjustmentReasonCode": "10"
}, {
      "adjustmentAmount": 444444.44,
      "adjustmentStartDate": "2013-03-01",
      "adjustmentEndDate": "2013-11-01",
      "adjustmentReasonCode": "10"
} ]
}
      """

  def paymentDetails(identifier: String): Action[AnyContent] = Action { implicit request =>

    identifier match {
      case "AB654321"  => Ok(AB654321).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")
      case "YY123499D" => Ok(YY123499D).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")

    }

  }
}
