package models.common

import base.BaseSpec
import play.api.libs.json.Json

class NationalInsuranceNumberSpec extends BaseSpec {
  "NationalInsuranceNumber" - {
    "GIVEN a valid nino" - {
      "THEN the expected NationalInsuranceNumber is returned" in {
        forAll(generateNino) { nino =>
          val result = NationalInsuranceNumber(nino)

          result.nino mustBe nino
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateNino) { nino =>
        val nationalInsuranceNumber = NationalInsuranceNumber(nino)
        Json.toJson(nationalInsuranceNumber)
      }
    }
  }
}
