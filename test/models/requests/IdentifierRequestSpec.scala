/*
 * Copyright 2024 HM Revenue & Customs
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

import base.BaseSpec
import models.common.NationalInsuranceNumber
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.GET

class IdentifierRequestSpec extends BaseSpec {
  "IdentifierRequest" - {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "unit-test/route")
    "GIVEN a valid request, national insurance number, whether the user is an individual and an internal id" - {
      "THEN the expected IdentifierRequest is returned" in {
        forAll(arbitrary[NationalInsuranceNumber], arbitrary[Boolean], generateId) { (nino, isIndividual, internalId) =>
          val result = IdentifierRequest(request, nino, isIndividual, internalId)

          result.request mustBe request
          result.nino mustBe nino
          result.isIndividual mustBe isIndividual
          result.internalId mustBe internalId
        }
      }
    }
  }
}
