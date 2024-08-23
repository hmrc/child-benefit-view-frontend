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

import forms.behaviours.StringFieldBehaviours
import models.cob.NewAccountDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.{FakeRequest, Injecting}

class NewAccountDetailsFormProviderSpec
    extends AnyFreeSpec
    with StringFieldBehaviours
    with GuiceOneAppPerSuite
    with Injecting {

  val form = new NewAccountDetailsFormProvider()()

  ".newAccountHoldersName" - {

    val fieldName   = "newAccountHoldersName"
    val requiredKey = "newAccountDetails.error.newAccountHoldersName.required"
    val lengthKey   = "newAccountDetails.error.newAccountHoldersName.length"
    val maxLength   = 60

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonNumerics suchThat (_.length <= maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength => nonNumerics suchThat (_.length > maxLength)
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

  ".newAccountNumber" - {

    val fieldName   = "newAccountNumber"
    val requiredKey = "newAccountDetails.error.newAccountNumber.required"
    val lengthKey   = "newAccountDetails.error.newAccountNumber.length"
    val minLength   = 8
    val maxLength   = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      numericalString(maxLength)
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, lengthKey, Seq(minLength)),
      maxLength => numericalString(maxLength - 1)
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

  ".bacsError" - {

    lazy val messagesApi: MessagesApi = inject[MessagesApi]

    implicit def messages: Messages = messagesApi.preferred(FakeRequest())

    val newAccountDetails: NewAccountDetails = NewAccountDetails("name", "123456", "11110000")

    Seq(
      ("priority1", "Sort code not found — check the sort code"),
      ("priority2", "Account not found — check the sort code and account number"),
      (
        "priority3",
        "You cannot use this service for this type of account — enter details of a personal " +
          "bank account or a building society without a roll number"
      ),
      ("priority4", "Account does not accept direct credit transfer — enter different account details"),
      ("priority5", "Account cannot be verified — check the account name, sort code and account number"),
      ("priority6", "Account name does not match account details — check account name")
    ) foreach {
      case (priority, errorText) =>
        s"should not bind $priority values and return $errorText" in {
          val formErrorMessage =
            form
              .bind(
                Map(
                  "bacsError"             -> priority,
                  "newSortCode"           -> newAccountDetails.newSortCode,
                  "newAccountHoldersName" -> newAccountDetails.newAccountHoldersName,
                  "newAccountNumber"      -> newAccountDetails.newAccountNumber
                )
              )
              .errors
              .head
              .message

          messages(formErrorMessage) mustBe errorText
        }
    }
  }
}
