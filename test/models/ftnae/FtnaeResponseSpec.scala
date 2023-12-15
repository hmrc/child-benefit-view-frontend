package models.ftnae

import base.BaseSpec
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class FtnaeResponseSpec extends BaseSpec {
  "FtnaeResponse" - {
    "GIVEN a valid claimant info and a list of ftnae child info" - {
      "THEN the expected FtnaeResponse is returned" in {
        forAll(arbitrary[FtnaeClaimantInfo], arbitrary[List[FtnaeChildInfo]]) { (claimantInfo, childInfoList) =>
          val result = FtnaeResponse(claimantInfo, childInfoList)

          result.claimant mustBe claimantInfo
          result.children mustBe childInfoList
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[FtnaeClaimantInfo], arbitrary[List[FtnaeChildInfo]]) { (claimantInfo, childInfoList) =>
        val ftnaeResponse = FtnaeResponse(claimantInfo, childInfoList)
        Json.toJson(ftnaeResponse)
      }
    }
  }
}
