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

package models.audit

import models.changeofbank.{AccountHolderName, BankAccountNumber, BuildingSocietyRollNumber, SortCode}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class PersonalInformation(name: String, dateOfBirth: LocalDate, nino: String)

object PersonalInformation {
  implicit val formatPersonalInformation: OFormat[PersonalInformation] = Json.format[PersonalInformation]
}

final case class BankDetails(
    firstname:                 String,
    surname:                   String,
    accountHolderName:         Option[AccountHolderName],
    accountNumber:             Option[BankAccountNumber],
    sortCode:                  Option[SortCode],
    buildingSocietyRollNumber: Option[BuildingSocietyRollNumber]
)

object BankDetails {
  implicit val formatBankDetails: OFormat[BankDetails] = Json.format[BankDetails]
}

final case class ViewDetails(
    accountHolderName: String,
    accountNumber:     String,
    sortCode:          String
)

object ViewDetails {
  implicit val formatViewDetails: OFormat[ViewDetails] = Json.format[ViewDetails]
}

final case class ChangeOfBankAccountDetailsModel(
    nino:                String,
    status:              String,
    referrer:            String,
    deviceFingerprint:   String,
    personalInformation: PersonalInformation,
    bankDetails:         BankDetails,
    viewDetails:         ViewDetails
)

object ChangeOfBankAccountDetailsModel {
  implicit val formatChangeofBankAccountDetailsModel: OFormat[ChangeOfBankAccountDetailsModel] =
    Json.format[ChangeOfBankAccountDetailsModel]
  val EventType: String = "ChangeOfBankAccountDetails"
}
