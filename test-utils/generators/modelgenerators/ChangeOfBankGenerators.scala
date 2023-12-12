package generators.modelgenerators

import generators.DataGenerators
import models.changeofbank._
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ChangeOfBankGenerators extends DataGenerators with CommonGenerators {
  implicit lazy val arbitraryAccountHolderName: Arbitrary[AccountHolderName] =
    Arbitrary {
      for {
        accountHolderName <- generateName
      } yield AccountHolderName(accountHolderName)
    }
  implicit lazy val arbitraryAccountHolderType: Arbitrary[AccountHolderType] =
    Arbitrary {
      Gen.oneOf(AccountHolderType.values)
    }
  implicit lazy val arbitraryBankAccountNumber: Arbitrary[BankAccountNumber] =
    Arbitrary {
      for {
        accountNumber <- generateAccountNumber
      } yield BankAccountNumber(accountNumber)
    }
  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountHolderType <- Arbitrary.arbitrary[AccountHolderType]
        accountHolderName <- arbitrary[AccountHolderName]
        bankAccountNumber <- arbitrary[BankAccountNumber]
        sortCode          <- arbitrary[SortCode]
      } yield BankDetails(accountHolderType, accountHolderName, bankAccountNumber, sortCode)
    }
  implicit lazy val arbitraryBuildingSocietyRollNumber: Arbitrary[BuildingSocietyRollNumber] =
    Arbitrary {
      for {
        numerical <- generateBuildingSocietyNumber
      } yield BuildingSocietyRollNumber(numerical)
    }
  implicit lazy val arbitraryClaimantBankAccountInformation: Arbitrary[ClaimantBankAccountInformation] =
    Arbitrary {
      for {
        name          <- arbitrary[AccountHolderName]
        sortCode      <- arbitrary[SortCode]
        accountNumber <- arbitrary[BankAccountNumber]
        rollNumber    <- arbitrary[BuildingSocietyRollNumber]
      } yield ClaimantBankAccountInformation(Some(name), Some(sortCode), Some(accountNumber), Some(rollNumber))
    }
  implicit lazy val arbitraryClaimantBankInformation: Arbitrary[ClaimantBankInformation] =
    Arbitrary {
      for {
        firstForename    <- arbitrary[FirstForename]
        surname          <- arbitrary[Surname]
        dateOfBirth      <- arbitrary[LocalDate]
        activeClaim      <- arbitrary[Boolean]
        financialDetails <- arbitrary[ClaimantFinancialDetails]
      } yield ClaimantBankInformation(firstForename, surname, dateOfBirth, activeClaim, financialDetails)
    }
  implicit lazy val arbitraryClaimantFinancialDetails: Arbitrary[ClaimantFinancialDetails] =
    Arbitrary {
      for {
        awardEndDate            <- arbitrary[LocalDate]
        claimantBankInformation <- arbitrary[ClaimantBankAccountInformation]
      } yield ClaimantFinancialDetails(awardEndDate, None, None, claimantBankInformation)
    }
  implicit lazy val arbitrarySortCode: Arbitrary[SortCode] =
    Arbitrary {
      for {
        sortCode <- generateSortCode
      } yield SortCode(sortCode)
    }
}
