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

package generators

import generators.modelgenerators._
import org.scalacheck.Gen
import play.api.http.Status._

trait ModelGenerators
    extends DataGenerators
    with AuditGenerators
    with EntitlementGenerators
    with ChangeOfBankGenerators
    with CoBGenerators
    with CommonGenerators
    with FTNAEGenerators {
  val randomFailureStatusCode: Gen[Int] =
    Gen.oneOf(
      BAD_REQUEST,
      UNAUTHORIZED,
      FORBIDDEN,
      NOT_FOUND,
      INTERNAL_SERVER_ERROR,
      NOT_IMPLEMENTED,
      SERVICE_UNAVAILABLE
    )

}
