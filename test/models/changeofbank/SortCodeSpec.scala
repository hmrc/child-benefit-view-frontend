package models.changeofbank

import base.BaseSpec
import play.api.libs.json.Json

class SortCodeSpec extends BaseSpec {
  "SortCode" - {
    "GIVEN a valid value" - {
      "THEN the expected SortCode is returned" in {
        forAll(generateSortCode) { number =>
          val result = SortCode(number)

          result.value mustBe number
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateSortCode) { number =>
        val sortCode = BuildingSocietyRollNumber(number)
        Json.toJson(sortCode)
      }
    }
  }
}
