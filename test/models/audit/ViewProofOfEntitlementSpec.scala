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
