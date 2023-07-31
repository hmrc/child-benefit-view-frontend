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

package utils.helpers

import models.changeofbank.{BankAccountNumber, ClaimantBankInformation, SortCode}
import models.common.{FirstForename, Surname}
import play.api.i18n.Messages
import utils.helpers.StringHelper.toTitleCase

object ClaimantBankInformationHelper {
  val accountNumberTake = 4

  def formatClaimantBankInformation(information: ClaimantBankInformation): ClaimantBankInformation =
    information.copy(
      firstForename = FirstForename(toTitleCase(information.firstForename.value)),
      surname = Surname(toTitleCase(information.surname.value))
    )

  def formatBankAccountInformation(
      information:     ClaimantBankInformation
  )(implicit messages: Messages): ClaimantBankInformation =
    information.copy(
      financialDetails = information.financialDetails.copy(
        bankAccountInformation = information.financialDetails.bankAccountInformation.copy(
          sortCode = information.financialDetails.bankAccountInformation.sortCode
            .map(s => SortCode(s.value.grouped(2).reduce((prev, next) => s"$prev-$next"))),
          bankAccountNumber = information.financialDetails.bankAccountInformation.bankAccountNumber
            .map(n =>
              BankAccountNumber(
                s"${messages("changeAccount.table.ending.in")} ${n.number.takeRight(accountNumberTake)}"
              )
            )
        )
      )
    )
}
