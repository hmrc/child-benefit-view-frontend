package models.common

import base.BaseSpec
import play.api.libs.json.Json

class ChildReferenceNumberSpec extends BaseSpec {
  "ChildReferenceNumber" - {
    "GIVEN a valid value" - {
      "THEN the expected ChildReferenceNumber is returned" in {
        forAll(generateReferenceNumber) { value =>
          val result = ChildReferenceNumber(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateReferenceNumber) { value =>
        val childReferenceNumber = ChildReferenceNumber(value)
        Json.toJson(childReferenceNumber)
      }
    }
  }
}
