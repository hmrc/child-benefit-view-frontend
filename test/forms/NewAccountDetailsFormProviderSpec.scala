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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.cob.NewAccountDetailsFormProvider
import play.api.data.FormError

class NewAccountDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new NewAccountDetailsFormProvider()()

  ".newAccountHoldersName" - {

    val fieldName   = "newAccountHoldersName"
    val requiredKey = "newAccountDetails.error.newAccountHoldersName.required"
    val lengthKey   = "newAccountDetails.error.newAccountHoldersName.length"
    val maxLength   = 60

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonNumerics suchThat (_.size <= maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength => nonNumerics suchThat (_.size > maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".newSortCode" - {

    val fieldName   = "newSortCode"
    val requiredKey = "newAccountDetails.error.newSortCode.required"
    val lengthKey   = "newAccountDetails.error.newSortCode.format"
    val maxLength   = 6

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      numericalString(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength => numericalString(maxLength + 1)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
