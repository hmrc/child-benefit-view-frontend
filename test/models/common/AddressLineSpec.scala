package models.common

import base.BaseSpec
import play.api.libs.json.Json

class AddressLineSpec extends BaseSpec {
  "AddressLine" - {
    "GIVEN a valid value" - {
      "THEN the expected AddressLine is returned" in {
        forAll(generateAddressLine) { value =>
          val result = AddressLine(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateAddressLine) { value =>
        val addressLine = AddressLine(value)
        Json.toJson(addressLine)
      }
    }
  }
}
