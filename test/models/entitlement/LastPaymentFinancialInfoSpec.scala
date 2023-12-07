package models.entitlement

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class LastPaymentFinancialInfoSpec extends BaseSpec {
  "LastPaymentFinancialInfo" - {
    "GIVEN a credit date and credit amount" - {
      "THEN the expected LastPaymentFinancialInfo is returned" in {
        forAll(arbitrary[LocalDate], arbitrary[BigDecimal]) { (creditDate, creditAmount) =>
          val result = LastPaymentFinancialInfo(creditDate, creditAmount)

          result.creditDate mustBe creditDate
          result.creditAmount mustBe creditAmount
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[LocalDate], arbitrary[BigDecimal]) { (creditDate, creditAmount) =>
        val lastPaymentFinancialInfo = LastPaymentFinancialInfo(creditDate, creditAmount)
        Json.toJson(lastPaymentFinancialInfo)
      }
    }
  }
}
