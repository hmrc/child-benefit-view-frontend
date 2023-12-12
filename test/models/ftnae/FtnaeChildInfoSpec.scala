package models.ftnae

import base.BaseSpec
import models.common.{ChildReferenceNumber, FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

import java.time.LocalDate

class FtnaeChildInfoSpec extends BaseSpec {
  "FtnaeChildInfo" - {
    "GIVEN a valid child reference number, name, surname, date of birth, claim end date" - {
      forAll(trueFalseCases) { withMidName =>
        s"AND a mid name ${isOrIsNot(withMidName)} provided" - {
          "THEN the expected FtnaeChildInfo is returned" in {
            forAll(
              arbitrary[ChildReferenceNumber],
              arbitrary[FirstForename],
              arbitrary[SecondForename],
              arbitrary[Surname],
              arbitrary[LocalDate],
              arbitrary[LocalDate]
            ) { (referenceNumber, firstName, midName, surname, dateOfBirth, endDate) =>
              val result = FtnaeChildInfo(
                referenceNumber,
                firstName,
                if (withMidName) Some(midName) else None,
                surname,
                dateOfBirth,
                endDate
              )

              result.crn mustBe referenceNumber
              result.name mustBe firstName
              result.midName mustBe (if (withMidName) Some(midName) else None)
              result.lastName mustBe surname
              result.dateOfBirth mustBe dateOfBirth
              result.currentClaimEndDate mustBe endDate
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[ChildReferenceNumber],
        arbitrary[FirstForename],
        arbitrary[SecondForename],
        arbitrary[Surname],
        arbitrary[LocalDate],
        arbitrary[LocalDate]
      ) { (referenceNumber, firstName, midName, surname, dateOfBirth, endDate) =>
        val ftnaeChildInfo = FtnaeChildInfo(referenceNumber, firstName, Some(midName), surname, dateOfBirth, endDate)
        Json.toJson(ftnaeChildInfo)
      }
    }
  }
}
