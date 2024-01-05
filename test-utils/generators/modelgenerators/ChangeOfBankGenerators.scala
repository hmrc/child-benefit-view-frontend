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
