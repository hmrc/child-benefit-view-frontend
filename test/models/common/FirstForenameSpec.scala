package models.common

import base.BaseSpec
import play.api.libs.json.Json

class FirstForenameSpec extends BaseSpec {
  "FirstForename" - {
    "GIVEN a valid value" - {
      "THEN the expected FirstForename is returned" in {
        forAll(generateName) { value =>
          val result = FirstForename(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName) { value =>
        val firstForename = FirstForename(value)
        Json.toJson(firstForename)
      }
    }
  }
}
