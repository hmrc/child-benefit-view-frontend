package models.common

import base.BaseSpec
import play.api.libs.json.Json

class SurnameSpec extends BaseSpec {
  "Surname" - {
    "GIVEN a valid value" - {
      "THEN the expected Surname is returned" in {
        forAll(generateName) { value =>
          val result = Surname(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName) { value =>
        val surname = Surname(value)
        Json.toJson(surname)
      }
    }
  }
}
