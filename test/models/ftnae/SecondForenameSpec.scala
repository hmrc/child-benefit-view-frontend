package models.ftnae

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class SecondForenameSpec extends BaseSpec {
  "SecondForename" - {
    "GIVEN a valid value" - {
      "THEN the expected SecondForename is returned" in {
        forAll(alphaStr) { value =>
          val result = SecondForename(value)

          result.value mustBe value
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(alphaStr) { value =>
        val secondForename = SecondForename(value)
        Json.toJson(secondForename)
      }
    }
  }
}
