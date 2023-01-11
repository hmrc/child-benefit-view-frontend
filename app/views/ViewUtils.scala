/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import play.api.data.Form
import play.api.i18n.Messages
import services.PaymentHistoryPageVariant
import services.PaymentHistoryPageVariant._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""
  }

  def formatDate(
      date:            LocalDate
  )(implicit messages: Messages): String = {
    date.format(
      DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.locale)
    )
  }

  def formatMoney(amount: BigDecimal, currency: String = "£"): String =
    f"$currency${amount.setScale(2)}"

  def navigatePaymentHistory(pageVariant: PaymentHistoryPageVariant): String =
    pageVariant match {
      case InPaymentWithPaymentsInLastTwoYears                => "payment details - active - payments"
      case InPaymentWithoutPaymentsInLastTwoYears             => "payment details - active - no payments"
      case HICBCWithPaymentsInLastTwoYears                    => "payment details - hicbc - payments"
      case HICBCWithoutPaymentsInLastTwoYears                 => "payment details - hicbc - no payments"
      case HICBCWithoutPaymentsInLastTwoYearsAndEndDateInPast => "payment details - active - no payments"
      case EntitlementEndedButReceivedPaymentsInLastTwoYears  => "payment details - inactive - payments"
      case EntitlementEndedButNoPaymentsInLastTwoYears        => "payment details - inactive - no payments"
    }

  def formatSensitiveSort(raw: String): String = {
    s"""**-**-${raw.filter(_.isDigit).substring(4)}"""
  }

  def formatSensitiveAccNumber(raw: String): String = {
    val asterisks: String = (for (_ <- 1 to (raw.length - 4)) yield "*").mkString
    s"""$asterisks${raw.takeRight(4)}"""
  }

}
