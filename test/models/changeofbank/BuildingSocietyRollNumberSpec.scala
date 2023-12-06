package models.changeofbank

import base.BaseSpec
import org.scalacheck.Gen.numStr
import play.api.libs.json.Json

class BuildingSocietyRollNumberSpec extends BaseSpec {
  "BuildingSocietyRollNumber" - {
      "GIVEN a valid value" - {
        "THEN the expected BuildingSocietyRollNumber is returned" in {
          forAll(generateBuildingSocietyNumber) { number =>
            val result = BuildingSocietyRollNumber(number)

            result.value mustBe number
          }
        }
      }
      "format: should successfully format to JSON" in {
        forAll(numStr) { number =>
          val buildingSocietyRollNumber = BuildingSocietyRollNumber(number)
          Json.toJson(buildingSocietyRollNumber)
        }
      }
  }
}
