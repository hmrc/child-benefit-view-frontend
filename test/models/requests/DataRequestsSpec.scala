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
