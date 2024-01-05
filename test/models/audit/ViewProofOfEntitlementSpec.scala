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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaNumStr, alphaStr}
import play.api.libs.json.Json

class ViewProofOfEntitlementSpec extends BaseSpec {
  "ViewProofOfEntitlement" - {
    "GIVEN a valid nino, status, referrer and deviceFingerprint" - {
      forAll(trueFalseCases) { withDetails =>
        s"AND child claimant entitlement details ${isOrIsNot(withDetails)} provided" - {
          "THEN the expected ViewProofOfEntitlementModel is returned" in {
            forAll(generateNino, alphaStr, alphaStr, alphaNumStr, arbitrary[ClaimantEntitlementDetails]) {
              (nino, status, referrer, deviceFingerprint, details) =>
                val result = ViewProofOfEntitlementModel(
                  nino,
                  status,
                  referrer,
                  deviceFingerprint,
                  if (withDetails) Some(details) else None
                )

                result.nino mustBe nino
                result.status mustBe status
                result.referrer mustBe referrer
                result.deviceFingerprint mustBe deviceFingerprint
                result.claimantEntitlementDetails mustBe (if (withDetails) Some(details) else None)
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateNino, alphaStr, alphaStr, alphaNumStr, arbitrary[ClaimantEntitlementDetails]) {
        (nino, status, referrer, deviceFingerprint, details) =>
          val viewProofOfEntitlementModel =
            ViewProofOfEntitlementModel(nino, status, referrer, deviceFingerprint, Some(details))
          Json.toJson(viewProofOfEntitlementModel)
      }
    }
  }
}
