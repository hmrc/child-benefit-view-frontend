/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        forAll(
          generateNino,
          alphaStr,
          alphaStr,
          alphaNumStr,
          intsAboveValue(0),
          arbitrary[Seq[LastPaymentFinancialInfo]]
        ) { (nino, status, referrer, deviceFingerprint, numOfPayments, payments) =>
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
      forAll(
        generateNino,
        alphaStr,
        alphaStr,
        alphaNumStr,
        intsAboveValue(0),
        arbitrary[Seq[LastPaymentFinancialInfo]]
      ) { (nino, status, referrer, deviceFingerprint, numOfPayments, payments) =>
        val viewPaymentDetailsModel =
          ViewPaymentDetailsModel(nino, status, referrer, deviceFingerprint, numOfPayments, payments)
        Json.toJson(viewPaymentDetailsModel)
      }
    }
  }
}
