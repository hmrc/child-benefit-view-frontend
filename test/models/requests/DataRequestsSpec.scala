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
import models.UserAnswers
import models.common.NationalInsuranceNumber
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.GET

class DataRequestsSpec extends BaseSpec {
  "OptionalDataRequest" - {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "unit-test/route")
    "GIVEN a valid request, userId, national insurance number" - {
      forAll(trueFalseCases) { withUserAnswers =>
        s"AND user answers ${areOrAreNot(withUserAnswers)} provided" - {
          "THEN the expected OptionalDataRequest is returned" in {
            forAll(generateId, arbitrary[NationalInsuranceNumber], arbitrary[UserAnswers]) {
              (userid, nino, userAnswers) =>
                val result = OptionalDataRequest(
                  request,
                  userid,
                  nino,
                  if (withUserAnswers) Some(userAnswers) else None
                )

                result.request mustBe request
                result.userId mustBe userid
                result.nino mustBe nino
                result.userAnswers mustBe (if (withUserAnswers) Some(userAnswers) else None)
            }
          }
        }
      }
    }
  }
  "DataRequest" - {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "unit-test/route")
    "GIVEN a valid request, userId, national insurance number" - {
      "THEN the expected DataRequest is returned" in {
        forAll(generateId, arbitrary[NationalInsuranceNumber], arbitrary[UserAnswers]) { (userid, nino, userAnswers) =>
          val result = DataRequest(request, userid, nino, userAnswers)

          result.request mustBe request
          result.userId mustBe userid
          result.nino mustBe nino
          result.userAnswers mustBe userAnswers
        }
      }
    }
  }
  "FtnaePaymentsExtendedPageDataRequest" - {
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "unit-test/route")
    "GIVEN a valid request, userId, national insurance number" - {
      "THEN the expected FtnaePaymentsExtendedPageDataRequest is returned" in {
        forAll(generateId, arbitrary[NationalInsuranceNumber], arbitrary[UserAnswers]) { (userid, nino, userAnswers) =>
          val result = FtnaePaymentsExtendedPageDataRequest(request, userid, nino, userAnswers)

          result.request mustBe request
          result.userId mustBe userid
          result.nino mustBe nino
          result.userAnswers mustBe userAnswers
        }
      }
    }
  }
}
