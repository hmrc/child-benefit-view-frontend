package models.changeofbank

import base.BaseSpec
import org.scalacheck.Gen.numStr
import play.api.libs.json.Json

class BankAccountNumberSpec extends BaseSpec {
  "BackAccountNumber" - {
    "GIVEN a valid number" - {
      "THEN the expected BackAccountNumber is returned" in {
        forAll(numStr) { number =>
          val result = BankAccountNumber(number)

          result.number mustBe number
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(numStr) { number =>
        val bankAccountNumber = BankAccountNumber(number)
        Json.toJson(bankAccountNumber)
      }
    }
  }
}
