/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.ct

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton()
final class CtController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def response(ctUtr: String): Action[AnyContent] = Action {
    ctUtr match {
      case "1097172564" => Ok(singleLivePeriod)
      case "1777802586" => Ok(twoLivePeriods)
      case "6437623340" => Ok(sixLivePeriod)
      case "3784008217" => Ok(tenLivePeriod)
      case "3600522978" => Ok(elevenLivePeriod)
      case "3600522888" => Ok(threeAnnualLivePeriods)
      case "3600522889" => Ok(twoAnnualAndOneMonthlyLivePeriods)
      case "8666000157" => Ok(JsArray(Seq.empty))
      case "1372613788" => NotFound
    }
  }

  private val singleLivePeriod: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-01",
        "periodEndDate" -> "2011-02-01")
    ))

  private val twoLivePeriods: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2012-01-09"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2014-01-10",
        "periodEndDate" -> "2015-01-09")
    ))

  private val sixLivePeriod: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2011-02-01"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2011-02-10",
        "periodEndDate" -> "2011-03-10"),
      Json.obj(
        "periodReference" -> 3,
        "periodStartDate" -> "2011-03-10",
        "periodEndDate" -> "2011-04-01"),
      Json.obj(
        "periodReference" -> 4,
        "periodStartDate" -> "2011-04-10",
        "periodEndDate" -> "2011-05-01"),
      Json.obj(
        "periodReference" -> 5,
        "periodStartDate" -> "2011-05-10",
        "periodEndDate" -> "2011-06-01"),
      Json.obj(
        "periodReference" -> 6,
        "periodStartDate" -> "2011-06-10",
        "periodEndDate" -> "2011-07-01")
    ))

  private val tenLivePeriod: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2011-02-01"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2011-02-10",
        "periodEndDate" -> "2011-03-10"),
      Json.obj(
        "periodReference" -> 3,
        "periodStartDate" -> "2011-03-10",
        "periodEndDate" -> "2011-04-01"),
      Json.obj(
        "periodReference" -> 4,
        "periodStartDate" -> "2011-04-10",
        "periodEndDate" -> "2011-05-01"),
      Json.obj(
        "periodReference" -> 5,
        "periodStartDate" -> "2011-05-10",
        "periodEndDate" -> "2011-06-01"),
      Json.obj(
        "periodReference" -> 6,
        "periodStartDate" -> "2011-06-10",
        "periodEndDate" -> "2011-07-01"),
      Json.obj(
        "periodReference" -> 7,
        "periodStartDate" -> "2011-07-10",
        "periodEndDate" -> "2011-08-01"),
      Json.obj(
        "periodReference" -> 8,
        "periodStartDate" -> "2011-08-10",
        "periodEndDate" -> "2011-09-01"),
      Json.obj(
        "periodReference" -> 9,
        "periodStartDate" -> "2011-09-10",
        "periodEndDate" -> "2011-10-01"),
      Json.obj(
        "periodReference" -> 10,
        "periodStartDate" -> "2011-10-10",
        "periodEndDate" -> "2011-11-01")
    ))

  private val elevenLivePeriod: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2011-02-01"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2011-02-10",
        "periodEndDate" -> "2011-03-10"),
      Json.obj(
        "periodReference" -> 3,
        "periodStartDate" -> "2011-03-10",
        "periodEndDate" -> "2011-04-01"),
      Json.obj(
        "periodReference" -> 4,
        "periodStartDate" -> "2011-04-10",
        "periodEndDate" -> "2011-05-01"),
      Json.obj(
        "periodReference" -> 5,
        "periodStartDate" -> "2011-05-10",
        "periodEndDate" -> "2011-06-01"),
      Json.obj(
        "periodReference" -> 6,
        "periodStartDate" -> "2011-06-10",
        "periodEndDate" -> "2011-07-01"),
      Json.obj(
        "periodReference" -> 7,
        "periodStartDate" -> "2011-07-10",
        "periodEndDate" -> "2011-08-01"),
      Json.obj(
        "periodReference" -> 8,
        "periodStartDate" -> "2011-08-10",
        "periodEndDate" -> "2011-09-01"),
      Json.obj(
        "periodReference" -> 9,
        "periodStartDate" -> "2011-09-10",
        "periodEndDate" -> "2011-10-01"),
      Json.obj(
        "periodReference" -> 10,
        "periodStartDate" -> "2011-10-10",
        "periodEndDate" -> "2011-11-01"),
      Json.obj(
        "periodReference" -> 11,
        "periodStartDate" -> "2011-11-10",
        "periodEndDate" -> "2011-12-01")
    ))

  private val threeAnnualLivePeriods: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2012-02-10"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2012-02-10",
        "periodEndDate" -> "2013-02-10"),
      Json.obj(
        "periodReference" -> 3,
        "periodStartDate" -> "2013-03-10",
        "periodEndDate" -> "2014-03-08"),
    ))

  private val twoAnnualAndOneMonthlyLivePeriods: JsArray =
    JsArray(Seq(
      Json.obj(
        "periodReference" -> 1,
        "periodStartDate" -> "2011-01-10",
        "periodEndDate" -> "2012-02-10"),
      Json.obj(
        "periodReference" -> 2,
        "periodStartDate" -> "2012-02-10",
        "periodEndDate" -> "2013-02-10"),
      Json.obj(
        "periodReference" -> 3,
        "periodStartDate" -> "2013-03-10",
        "periodEndDate" -> "2013-06-08"),
    ))

}

