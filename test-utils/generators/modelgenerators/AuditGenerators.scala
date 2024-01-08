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
import models.audit._
import models.changeofbank.{AccountHolderName, BankAccountNumber, BuildingSocietyRollNumber, SortCode}
import models.entitlement.Child
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDate

trait AuditGenerators extends ChangeOfBankGenerators with EntitlementGenerators with DataGenerators {

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
        name      <- generateName
        address   <- generateAddressLine
        amount    <- arbitrary[Double]
        startDate <- arbitrary[LocalDate]
        endDate   <- arbitrary[LocalDate]
        children  <- arbitrary[Seq[Child]]
      } yield ClaimantEntitlementDetails(name, address, amount, startDate.toString, endDate.toString, children)
    }
}
