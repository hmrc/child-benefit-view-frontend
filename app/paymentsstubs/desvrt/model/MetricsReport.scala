/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt.model

import play.api.libs.json.{Json, Writes}

case class MetricsReport(header: HeaderMetricsReport, transactions: TransactionsMetricsReport)

case class HeaderMetricsReport(
    count:                        Long,
    totalNetAmountInPence:        Long,
    totalCommissionAmountInPence: Long,
    totalGrossAmountInPence:      Long)

case class TransactionsMetricsReport(
    count:                        Long,
    totalNetAmountInPence:        Long,
    totalCommissionAmountInPence: Long,
    totalGrossAmountInPence:      Long,
    byCardClass:                  Seq[MetricsReportByGroup],
    byTaxType:                    Seq[MetricsReportByGroup],
    byTaxTypeAndCardClass:        Seq[MetricsReportByGroup])

case class MetricsReportByGroup(
    groupName:                    String,
    numberOfPayments:             Long,
    totalNetAmountInPence:        Long,
    totalCommissionAmountInPence: Long,
    totalGrossAmountInPence:      Long)

object MetricsReport {
  implicit val writes: Writes[MetricsReport] = Json.writes[MetricsReport]
}

object HeaderMetricsReport {
  implicit val writes: Writes[HeaderMetricsReport] = Json.writes[HeaderMetricsReport]
}

object TransactionsMetricsReport {
  implicit val writes: Writes[TransactionsMetricsReport] = Json.writes[TransactionsMetricsReport]
}

object MetricsReportByGroup {
  implicit val metricsReportByGroup: Writes[MetricsReportByGroup] = Json.writes[MetricsReportByGroup]
}
