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

package forms

import javax.inject.Inject

import utils.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.NewAccountDetails

class NewAccountDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[NewAccountDetails] =
    Form(
      mapping(
        "newAccountHoldersName" -> text("newAccountDetails.error.newAccountHoldersName.required")
          .verifying(maxLength(512, "newAccountDetails.error.newAccountHoldersName.length")),
        "newSortCode" -> text("newAccountDetails.error.newSortCode.required")
          .verifying(maxLength(6, "newAccountDetails.error.newSortCode.length")),
        "newAccountNumber" -> text("newAccountDetails.error.newAccountNumber.required")
          .verifying(maxLength(24, "newAccountDetails.error.newAccountNumber.length"))
      )(NewAccountDetails.apply)(NewAccountDetails.unapply)
    )
}
