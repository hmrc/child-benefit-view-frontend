package generators.modelgenerators

import generators.DataGenerators
import models.changeofbank.{AccountHolderName, BankAccountNumber, BuildingSocietyRollNumber, SortCode}
import org.scalacheck.Arbitrary

trait ChangeOfBankGenerators extends DataGenerators {
  implicit lazy val arbitraryAccountHolderName: Arbitrary[AccountHolderName] =
    Arbitrary {
      for {
        accountHolderName <- generateName
      } yield AccountHolderName(accountHolderName)
    }
  implicit lazy val arbitrarySortCode: Arbitrary[SortCode] =
    Arbitrary {
      for {
        sortCode <- generateSortCode
      } yield SortCode(sortCode)
    }
  implicit lazy val arbitraryBankAccountNumber: Arbitrary[BankAccountNumber] =
    Arbitrary {
      for {
        accountNumber <- generateAccountNumber
      } yield BankAccountNumber(accountNumber)
    }
  implicit lazy val arbitraryBuildingSocietyRollNumber: Arbitrary[BuildingSocietyRollNumber] =
    Arbitrary {
      for {
        numerical <- generateBuildingSocietyNumber
      } yield BuildingSocietyRollNumber(numerical)
    }

}
