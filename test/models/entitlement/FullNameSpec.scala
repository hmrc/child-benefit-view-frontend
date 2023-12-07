package models.entitlement

import base.BaseSpec
import play.api.libs.json.Json

class FullNameSpec extends BaseSpec {
  "FullName" - {
    "GIVEN a valid value" - {
      "THEN the expected FullName is returned" in {
        forAll(generateName) { value =>
          val result = FullName(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName) { value =>
        val fullName = FullName(value)
        Json.toJson(fullName)
      }
    }
  }
}
