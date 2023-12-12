package models.changeofbank

import base.BaseSpec
import play.api.libs.json.Json

class AccountHolderNameSpec extends BaseSpec {
  "AccountHolderName" - {
    "GIVEN a valid value" - {
      "THEN the expected AccountHolderName is returned" in {
        forAll(generateName) { name =>
          val result = AccountHolderName(name)

          result.value mustBe name
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName) { name =>
        val accountHolderName = AccountHolderName(name)
        Json.toJson(accountHolderName)
      }
    }
  }
}
