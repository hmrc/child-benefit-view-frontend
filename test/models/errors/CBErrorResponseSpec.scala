package models.errors

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class CBErrorResponseSpec extends BaseSpec {
  "CBErrorResponse" - {
    "GIVEN a valid status and description" - {
      "THEN the expected CBErrorResponse is returned" in {
        forAll(randomFailureStatusCode, alphaStr) { (status, description) =>
          val result = CBErrorResponse(status, description)

          result.status mustBe status
          result.description mustBe description
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(randomFailureStatusCode, alphaStr) { (status, description) =>
        val cbErrorResponse = CBErrorResponse(status, description)
        Json.toJson(cbErrorResponse)
      }
    }
  }
}
