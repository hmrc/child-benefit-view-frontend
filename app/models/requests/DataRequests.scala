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

package models.requests

import play.api.mvc.{Request, WrappedRequest}
import models.UserAnswers
import models.common.NationalInsuranceNumber

case class OptionalDataRequest[A](
    request:     Request[A],
    userId:      String,
    nino:        NationalInsuranceNumber,
    userAnswers: Option[UserAnswers]
) extends WrappedRequest[A](request)

abstract class BaseDataRequest[A](baseRequest: Request[A]) extends WrappedRequest[A](baseRequest) {
  def request: Request[A] = baseRequest
  def userId:      String
  def nino:        NationalInsuranceNumber
  def userAnswers: UserAnswers
}

final case class DataRequest[A](
    override val request:     Request[A],
    override val userId:      String,
    override val nino:        NationalInsuranceNumber,
    override val userAnswers: UserAnswers
) extends BaseDataRequest[A](request)

final case class FtnaePaymentsExtendedPageDataRequest[A](
    override val request:     Request[A],
    override val userId:      String,
    override val nino:        NationalInsuranceNumber,
    override val userAnswers: UserAnswers
) extends BaseDataRequest[A](request)
