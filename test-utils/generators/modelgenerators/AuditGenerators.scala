package generators.modelgenerators

import generators.DataGenerators
import models.audit.{BankDetails, ClaimantEntitlementDetails, PersonalInformation, ViewDetails}
import models.changeofbank.{AccountHolderName, BankAccountNumber, BuildingSocietyRollNumber, SortCode}
import models.entitlement.Child
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDate

trait AuditGenerators
  extends ChangeOfBankGenerators
    with EntitlementGenerators
    with DataGenerators {

  implicit lazy val arbitraryPersonalInformation: Arbitrary[PersonalInformation] =
    Arbitrary {
      for {
        name        <- generateName
        dateOfBirth <- arbitrary[LocalDate]
        nino        <- generateNino
      } yield PersonalInformation(name, dateOfBirth, nino)
    }

  implicit lazy val arbitraryAuditBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        firstname         <- generateName
        surname           <- generateName
        accountHolderName <- arbitrary[AccountHolderName]
        accountNumber     <- arbitrary[BankAccountNumber]
        sortCode          <- arbitrary[SortCode]
        bsrNumber         <- arbitrary[BuildingSocietyRollNumber]
      } yield BankDetails(
        firstname,
        surname,
        Some(accountHolderName),
        Some(accountNumber),
        Some(sortCode),
        Some(bsrNumber)
      )
    }

  implicit lazy val arbitraryViewDetails: Arbitrary[ViewDetails] =
    Arbitrary {
      for {
        accountHolderName <- generateName
        accountNumber     <- generateAccountNumber
        sortCode          <- generateSortCode
      } yield ViewDetails(accountHolderName, accountNumber, sortCode)
    }

  implicit lazy val arbitraryClaimantEntitlementDetails: Arbitrary[ClaimantEntitlementDetails] =
    Arbitrary {
      for {
        name <- generateName
        address <- generateAddressLine
        amount <- arbitrary[Double]
        startDate <- arbitrary[LocalDate]
        endDate <- arbitrary[LocalDate]
        children <- arbitrary[Seq[Child]]
      } yield ClaimantEntitlementDetails(name, address, amount, startDate.toString, endDate.toString, children)
    }
}
