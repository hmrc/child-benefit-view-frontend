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

package models.errors

trait CBError {
  val statusCode: Int
  val message:    String
}

final case class ConnectorError(statusCode: Int, message: String) extends CBError
final case class PaymentHistoryValidationError(statusCode: Int, message: String) extends CBError
final case class ChangeOfBankValidationError(statusCode: Int, message: String = "change of bank validation failed")
    extends CBError

final case class PriorityBARSVerificationError(statusCode: Int, message: String) extends CBError
final case class ClaimantIsLockedOutOfChangeOfBank(
    statusCode: Int,
    message:    String
) extends CBError

case object FtnaeNoCHBAccountError extends CBError {
  override val statusCode = 404
  override val message    = "No ChB Account"
}

case object FtnaeCannotFindYoungPersonError extends CBError {
  override val statusCode = 404
  override val message    = "Can not find young person"
}

case object FtnaeChildUserAnswersNotRetrieved extends CBError {
  override val statusCode = 500
  override val message    = "Unable to retrieve required user answers"
}
