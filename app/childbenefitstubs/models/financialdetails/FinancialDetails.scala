/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.financialdetails

import play.api.libs.json.{Format, Json}

case class FinancialDetails(debtIndicator:    Int,
                            bankData:         Option[BankData],
                            awardInformation: List[AwardInformation],
                            rateInformation:  Option[RateInformation],
                            paymentData:      Option[List[PaymentData]],
                            adjustmentType:   Option[List[AdjustmentType]])

object FinancialDetails {
  implicit val format: Format[FinancialDetails] = Json.format[FinancialDetails]
}
