/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import generators.modelgenerators._
import models.changeofbank._
import models.cob._
import models.common._
import models.ftnae._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen}
import play.api.http.Status._

import java.time.LocalDate

trait ModelGenerators
    extends DataGenerators
    with AuditGenerators
    with EntitlementGenerators
    with ChangeOfBankGenerators {

  implicit lazy val arbitraryHowManyYears: Arbitrary[HowManyYears] = {
    Arbitrary {
      Gen.oneOf(HowManyYears.values)
    }
  }

  implicit lazy val arbitraryNewAccountDetails: Arbitrary[NewAccountDetails] =
    Arbitrary {
      for {
        accountHolder <- generateName
        sortCode      <- generateSortCode
        accountNumber <- generateAccountNumber
      } yield NewAccountDetails(accountHolder, sortCode, accountNumber)
    }
  implicit lazy val arbitraryConfirmNewAccountDetails: Arbitrary[ConfirmNewAccountDetails] =
    Arbitrary {
      for {
        index <- arbitrary[Int].map(Math.abs(_))
      } yield ConfirmNewAccountDetails.values(index % ConfirmNewAccountDetails.values.length)
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
  implicit lazy val arbitraryFirstForename: Arbitrary[FirstForename] =
    Arbitrary {
      for {
        name <- generateName
      } yield FirstForename(name)
    }
  implicit lazy val arbitrarySecondForename: Arbitrary[SecondForename] =
    Arbitrary {
      for {
        name <- generateName
      } yield SecondForename(name)
    }
  implicit lazy val arbitrarySurname: Arbitrary[Surname] =
    Arbitrary {
      for {
        name <- generateName
      } yield Surname(name)
    }
  implicit lazy val arbitraryClaimantFinancialDetails: Arbitrary[ClaimantFinancialDetails] =
    Arbitrary {
      for {
        awardEndDate            <- arbitrary[LocalDate]
        claimantBankInformation <- arbitrary[ClaimantBankAccountInformation]
      } yield ClaimantFinancialDetails(awardEndDate, None, None, claimantBankInformation)
    }
  implicit lazy val arbitraryClaimantBankAccountInformation: Arbitrary[ClaimantBankAccountInformation] =
    Arbitrary {
      ClaimantBankAccountInformation(None, None, None, None)
    }
  implicit lazy val arbitraryAccountHolderType: Arbitrary[AccountHolderType] =
    Arbitrary {
      Gen.oneOf(AccountHolderType.values)
    }

  implicit lazy val arbitraryVerifyBankAccountRequest: Arbitrary[VerifyBankAccountRequest] =
    Arbitrary {
      for {
        accountHolderName <- arbitrary[AccountHolderName]
        sortCode          <- arbitrary[SortCode]
        bankAccountNumber <- arbitrary[BankAccountNumber]
      } yield VerifyBankAccountRequest(accountHolderName, sortCode, bankAccountNumber)
    }
  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =
    Arbitrary {
      for {
        accountHolderType <- arbitrary[AccountHolderType]
        accountHolderName <- arbitrary[AccountHolderName]
        bankAccountNumber <- arbitrary[BankAccountNumber]
        sortCode          <- arbitrary[SortCode]
      } yield BankDetails(accountHolderType, accountHolderName, bankAccountNumber, sortCode)
    }
  implicit lazy val arbitraryUpdateBankAccountRequest: Arbitrary[UpdateBankAccountRequest] = {
    Arbitrary {
      for {
        bankDetails <- arbitrary[BankDetails]
      } yield UpdateBankAccountRequest(bankDetails)
    }
  }
  implicit lazy val arbitraryChildReferenceNumber: Arbitrary[ChildReferenceNumber] =
    Arbitrary {
      for {
        referenceNumber <- generateReferenceNumber
      } yield ChildReferenceNumber(referenceNumber)
    }
  implicit lazy val arbitraryCourseDuration: Arbitrary[CourseDuration] =
    Arbitrary {
      Gen.oneOf(CourseDuration.OneYear, CourseDuration.TwoYear)
    }
  implicit lazy val arbitraryFtnaeQuestionAndAnswer: Arbitrary[FtnaeQuestionAndAnswer] =
    Arbitrary {
      for {
        question <- alphaStr
        answer   <- alphaStr
      } yield FtnaeQuestionAndAnswer(question, answer)
    }
  implicit lazy val arbitraryFtnaeClaimantInfo: Arbitrary[FtnaeClaimantInfo] =
    Arbitrary {
      for {
        firstname <- arbitrary[FirstForename]
        surname   <- arbitrary[Surname]
      } yield FtnaeClaimantInfo(firstname, surname)
    }
  implicit lazy val arbitraryChildDetails: Arbitrary[ChildDetails] =
    Arbitrary {
      for {
        courseDuration   <- arbitrary[CourseDuration]
        crn              <- arbitrary[ChildReferenceNumber]
        dateOfBirth      <- arbitrary[LocalDate]
        whichYoungPerson <- generateName
        ftnaeQA <-
          Gen.containerOf[List, FtnaeQuestionAndAnswer](arbitrary[FtnaeQuestionAndAnswer]) suchThat (x => x.nonEmpty)

      } yield ChildDetails(courseDuration, crn, dateOfBirth, whichYoungPerson, ftnaeQA)
    }
  implicit lazy val arbitraryFtnaeChildInfo: Arbitrary[FtnaeChildInfo] =
    Arbitrary {
      for {
        crn          <- arbitrary[ChildReferenceNumber]
        firstName    <- arbitrary[FirstForename]
        midName      <- arbitrary[SecondForename]
        lastName     <- arbitrary[Surname]
        dateOfBirth  <- arbitrary[LocalDate]
        claimEndDate <- arbitrary[LocalDate]
      } yield FtnaeChildInfo(crn, firstName, Some(midName), lastName, dateOfBirth, claimEndDate)
    }
  implicit lazy val arbitraryFtnaeResponse: Arbitrary[FtnaeResponse] =
    Arbitrary {
      for {
        claimant <- arbitrary[FtnaeClaimantInfo]
        children <- Gen.containerOf[List, FtnaeChildInfo](arbitrary[FtnaeChildInfo]) suchThat (x => x.nonEmpty)
      } yield FtnaeResponse(claimant, children)
    }

  val randomFailureStatusCode: Gen[Int] =
    Gen.oneOf(
      BAD_REQUEST,
      UNAUTHORIZED,
      FORBIDDEN,
      NOT_FOUND,
      INTERNAL_SERVER_ERROR,
      NOT_IMPLEMENTED,
      SERVICE_UNAVAILABLE
    )

}
