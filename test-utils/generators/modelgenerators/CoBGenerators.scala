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
