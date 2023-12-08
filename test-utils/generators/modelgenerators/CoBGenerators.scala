package generators.modelgenerators

import generators.DataGenerators
import models.changeofbank.{AccountHolderName, BankAccountNumber, BankDetails, SortCode}
import models.cob._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

trait CoBGenerators extends DataGenerators with ChangeOfBankGenerators {
  implicit lazy val arbitraryConfirmNewAccountDetails: Arbitrary[ConfirmNewAccountDetails] =
    Arbitrary {
      for {
        index <- arbitrary[Int].map(Math.abs(_))
      } yield ConfirmNewAccountDetails.values(index % ConfirmNewAccountDetails.values.length)
    }
  implicit lazy val arbitraryNewAccountDetails: Arbitrary[NewAccountDetails] =
    Arbitrary {
      for {
        accountHolder <- generateName
        sortCode      <- generateSortCode
        accountNumber <- generateAccountNumber
      } yield NewAccountDetails(accountHolder, sortCode, accountNumber)
    }
  implicit lazy val arbitraryUpdateBankAccountRequest: Arbitrary[UpdateBankAccountRequest] = {
    Arbitrary {
      for {
        bankDetails <- arbitrary[BankDetails]
      } yield UpdateBankAccountRequest(bankDetails)
    }
  }
  implicit lazy val arbitraryVerifyBankAccountRequest: Arbitrary[VerifyBankAccountRequest] =
    Arbitrary {
      for {
        accountHolderName <- arbitrary[AccountHolderName]
        sortCode          <- arbitrary[SortCode]
        bankAccountNumber <- arbitrary[BankAccountNumber]
      } yield VerifyBankAccountRequest(accountHolderName, sortCode, bankAccountNumber)
    }
}
