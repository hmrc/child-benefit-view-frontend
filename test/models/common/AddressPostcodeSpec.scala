package models.common

import base.BaseSpec
import play.api.libs.json.Json

class AddressPostcodeSpec extends BaseSpec {
  "AddressPostcode" - {
    "GIVEN a valid value" - {
      "THEN the expected AddressPostcode is returned" in {
        forAll(generatePostCode) { value =>
          val result = AddressPostcode(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generatePostCode) { value =>
        val addressPostcode = AddressPostcode(value)
        Json.toJson(addressPostcode)
      }
    }
  }
}
