package models.changeofbank

import base.BaseSpec
import play.api.libs.json.JsString

class AccountHolderTypeSpec extends BaseSpec {
  "AccountHolderType" - {
    val accountHolderToJSTestCases = Table(
      ("accountHolderType", "stringRepresentation"),
      (AccountHolderType.Claimant, "CLAIMANT"),
      (AccountHolderType.Joint, "JOINT"),
      (AccountHolderType.SomeoneElse, "SOMEONE_ELSE")
    )
    "GIVEN an AccountHolderType and an expected string value" - {
      forAll(accountHolderToJSTestCases) { (accountHolderType, stringRepresentation) =>
        s"Account holder type: $accountHolderType - expected value: $stringRepresentation" - {
          "THEN the AccountHolderType can be correctly written as a JSValue" in {
            val result = AccountHolderType.writes.writes(accountHolderType).toString

            result mustBe s""""$stringRepresentation""""
          }
          "THEN the string value can be correctly read as an AccountHolderType" in {
            val result = AccountHolderType.reads.reads(JsString(stringRepresentation))

            result.get mustBe accountHolderType
          }
        }
      }
    }
  }
}
