package models.ftnae

import base.BaseSpec
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class FtnaeClaimantInfoSpec extends BaseSpec {
  "FtnaeClaimantInfo" - {
    "GIVEN a valid name and surname" - {
      "THEN the expected FtnaeClaimantInfo is returned" in {
        forAll(arbitrary[FirstForename], arbitrary[Surname]) { (forename, surname) =>
          val result = FtnaeClaimantInfo(forename, surname)

          result.name mustBe forename
          result.surname mustBe surname
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(arbitrary[FirstForename], arbitrary[Surname]) { (forename, surname) =>
        val ftnaeClaimantInfo = FtnaeClaimantInfo(forename, surname)
        Json.toJson(ftnaeClaimantInfo)
      }
    }
  }
}
