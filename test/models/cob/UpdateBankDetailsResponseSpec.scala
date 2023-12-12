package models.cob

import base.BaseSpec
import org.scalacheck.Gen.alphaStr
import play.api.libs.json.Json

class UpdateBankDetailsResponseSpec extends BaseSpec {
  "UpdateBankDetailsResponse" - {
    "GIVEN a valid status" - {
      "THEN the expected UpdateBankDetailsResponse is returned" in {
        forAll(alphaStr) { status =>
          val result = UpdateBankDetailsResponse(status)

          result.status mustBe status
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(alphaStr) { status =>
        val updateBankDetailsResponse = UpdateBankDetailsResponse(status)
        Json.toJson(updateBankDetailsResponse)
      }
    }
  }
}
