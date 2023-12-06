package models.audit

import base.BaseSpec
import models.entitlement.LastPaymentFinancialInfo
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaNumStr, alphaStr}
import play.api.libs.json.Json

class ViewPaymentDetailsSpec extends BaseSpec {
  "ViewPaymentDetails" - {
    "GIVEN a valid nino, status, referrer, deviceFingerprint, number of payments visible to user and a sequence of payments" - {
      "THEN the expected ViewPaymentDetailsModel is returned" in {
        forAll(generateNino, alphaStr, alphaStr, alphaNumStr, intsAboveValue(0), arbitrary[Seq[LastPaymentFinancialInfo]]) {
          (nino, status, referrer, deviceFingerprint, numOfPayments, payments) =>
            val result = ViewPaymentDetailsModel(nino, status, referrer, deviceFingerprint, numOfPayments, payments)

            result.nino mustBe nino
            result.status mustBe status
            result.referrer mustBe referrer
            result.deviceFingerprint mustBe deviceFingerprint
            result.numberOfPaymentsVisibleToUser mustBe numOfPayments
            result.payments mustBe payments
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateNino, alphaStr, alphaStr, alphaNumStr, intsAboveValue(0), arbitrary[Seq[LastPaymentFinancialInfo]]) {
        (nino, status, referrer, deviceFingerprint, numOfPayments, payments) =>
          val viewPaymentDetailsModel = ViewPaymentDetailsModel(nino, status, referrer, deviceFingerprint, numOfPayments, payments)
          Json.toJson(viewPaymentDetailsModel)
      }
    }
  }
}
