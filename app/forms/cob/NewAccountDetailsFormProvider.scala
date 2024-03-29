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

package forms.cob

import models.cob.NewAccountDetails
import play.api.data.Form
import play.api.data.Forms._
import utils.mappings.BacsErrorMapping.bacsString
import utils.mappings.Mappings
import utils.mappings.SanitisedNumberMapping.sanitisedNumber

import javax.inject.Inject

class NewAccountDetailsFormProvider @Inject() extends Mappings {
  val nameMaxLength       = 60
  val sortCodeLength      = 6
  val accountNumberLength = 8

  def apply(): Form[NewAccountDetails] =
    Form(
      mapping(
        "newAccountHoldersName" -> text("newAccountDetails.error.newAccountHoldersName.required")
          .verifying(
            maxLength(nameMaxLength, "newAccountDetails.error.newAccountHoldersName.length"),
            pattern(
              """^[\w\s\-']+$""".r,
              "newAccountHoldersName.pattern",
              "newAccountDetails.error.newAccountHoldersName.format"
            )
          ),
        "newSortCode" -> sanitisedNumber(
          "newAccountDetails.error.newSortCode.required",
          "newAccountDetails.error.newSortCode.format"
        ).verifying(
          minLength(sortCodeLength, "newAccountDetails.error.newSortCode.format"),
          maxLength(sortCodeLength, "newAccountDetails.error.newSortCode.format")
        ),
        "newAccountNumber" -> sanitisedNumber(
          "newAccountDetails.error.newAccountNumber.required",
          "newAccountDetails.error.newAccountNumber.format"
        ).verifying(
          minLength(accountNumberLength, "newAccountDetails.error.newAccountNumber.length"),
          maxLength(accountNumberLength, "newAccountDetails.error.newAccountNumber.length")
        ),
        "bacsError" -> bacsString()
      )((a, b, c, _) => NewAccountDetails(a, b, c))(a =>
        Some((a.newAccountHoldersName, a.newSortCode, a.newAccountNumber, ""))
      )
    )
}
