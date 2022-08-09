/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class BankData(sortCode:          String,
                    bankAccountNumber: String,
                    accountHolderName: String,
                    validFromDate:     LocalDate)

object BankData {
  implicit val format: Format[BankData] = Json.format[BankData]
}
