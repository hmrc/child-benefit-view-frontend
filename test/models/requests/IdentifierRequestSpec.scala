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
