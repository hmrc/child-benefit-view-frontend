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

import models.changeofbank.{AccountHolderName, AccountHolderType, BankAccountNumber, BankDetails, ClaimantBankAccountInformation, ClaimantBankInformation, ClaimantFinancialDetails, SortCode}
import models.cob.{ConfirmNewAccountDetails, NewAccountDetails, UpdateBankAccountRequest, VerifyBankAccountRequest}
import models.common.{AddressLine, AddressPostcode, FirstForename, Surname}
import models.entitlement._
import models.ftnae.HowManyYears
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryHowManyYears: Arbitrary[HowManyYears] = {
    Arbitrary {
      Gen.oneOf(HowManyYears.values.toSeq)
    }
  }
  private val ACCOUNT_HOLDER_MAX_LENGTH     = 30
  private val ALLOWED_SORT_CODE_LENGTH      = 6
  private val ALLOWED_ACCOUNT_NUMBER_LENGTH = 8
  implicit lazy val arbitraryNewAccountDetails: Arbitrary[NewAccountDetails] =
    Arbitrary {
      for {
        accountHolder <- alphaStr.suchThat(_.length > 0).map(_.take(ACCOUNT_HOLDER_MAX_LENGTH))
        sortCode      <- numStr.map(_.take(ALLOWED_SORT_CODE_LENGTH))
        accountNumber <- numStr.map(_.take(ALLOWED_ACCOUNT_NUMBER_LENGTH))
      } yield NewAccountDetails(accountHolder, sortCode, accountNumber)
    }
  implicit lazy val arbitraryConfirmNewAccountDetails: Arbitrary[ConfirmNewAccountDetails] =
    Arbitrary {
      for {
        index <- arbitrary[Int].map(Math.abs(_))
      } yield ConfirmNewAccountDetails.values(index % ConfirmNewAccountDetails.values.length)
    }

  // Child Benefit Entitlements and associated model
  implicit lazy val genChildBenefitEntitlement: Gen[ChildBenefitEntitlement] =
    for {
      claimant <- arbitrary[Claimant]
      entitlementDate <- arbitrary[LocalDate]
      paidAmountForEldestOrOnlyChild <- arbitrary[BigDecimal]
      paidAmountForEachAdditionalChild <- arbitrary[BigDecimal]
      children <- Gen.containerOf[List, Child](arbitrary(arbitraryChild)) suchThat (x => x.nonEmpty)
    } yield ChildBenefitEntitlement(
      claimant,
      entitlementDate,
      paidAmountForEldestOrOnlyChild,
      paidAmountForEachAdditionalChild,
      children
    )
  implicit lazy val arbitraryChildBenefitEntitlement: Arbitrary[ChildBenefitEntitlement] =
    Arbitrary {
      for {
        claimant                         <- arbitrary[Claimant]
        entitlementDate                  <- arbitrary[LocalDate]
        paidAmountForEldestOrOnlyChild   <- arbitrary[BigDecimal]
        paidAmountForEachAdditionalChild <- arbitrary[BigDecimal]
        children                         <- Gen.containerOf[List, Child](arbitrary(arbitraryChild)) suchThat (x => x.nonEmpty)
      } yield ChildBenefitEntitlement(
        claimant,
        entitlementDate,
        paidAmountForEldestOrOnlyChild,
        paidAmountForEachAdditionalChild,
        children
      )
    }
  implicit lazy val arbitraryClaimant: Arbitrary[Claimant] =
    Arbitrary {
      for {
        fullName              <- arbitrary[FullName]
        awardValue            <- arbitrary[BigDecimal]
        awardStartDate        <- arbitrary[LocalDate]
        awardEndDate          <- arbitrary[LocalDate]
        higherRateValue       <- arbitrary[BigDecimal]
        standardRateValue     <- arbitrary[BigDecimal]
        lastPaymentInfo       <- Gen.containerOf[List, LastPaymentFinancialInfo](arbitrary(arbitraryLastPaymentFinancialInfo))
        fullAddress           <- arbitrary[FullAddress]
        adjustmentInformation <- arbitrary[Option[AdjustmentInformation]]
      } yield Claimant(
        fullName,
        awardValue,
        awardStartDate,
        awardEndDate,
        higherRateValue,
        standardRateValue,
        lastPaymentInfo,
        fullAddress,
        adjustmentInformation
      )
    }
  implicit lazy val arbitraryFullName: Arbitrary[FullName] =
    Arbitrary {
      for {
        givenName <- arbitrary[String]
        surname   <- arbitrary[String]
      } yield FullName(s"$givenName $surname")
    }
  implicit lazy val arbitraryLastPaymentFinancialInfo: Arbitrary[LastPaymentFinancialInfo] =
    Arbitrary {
      for {
        creditDate   <- arbitrary[LocalDate]
        creditAmount <- arbitrary[BigDecimal]
      } yield LastPaymentFinancialInfo(creditDate, creditAmount)
    }
  implicit lazy val arbitraryFullAddress: Arbitrary[FullAddress] =
    Arbitrary {
      for {
        addressLine1    <- arbitrary[AddressLine]
        addressLine2    <- arbitrary[AddressLine]
        addressLine3    <- arbitrary[Option[AddressLine]]
        addressLine4    <- arbitrary[Option[AddressLine]]
        addressLine5    <- arbitrary[Option[AddressLine]]
        addressPostCode <- arbitrary[AddressPostcode]
      } yield FullAddress(addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressPostCode)
    }
  implicit lazy val arbitraryAddressLine: Arbitrary[AddressLine] =
    Arbitrary {
      for {
        line <- alphaStr suchThat (_.length >= 25)
      } yield AddressLine(line)
    }
  implicit lazy val arbitraryPostcode: Arbitrary[AddressPostcode] =
    Arbitrary {
      for {
        a1 <- stringOf(alphaChar)
        a2 <- stringOf(alphaChar)
        n1 <- choose[Int](1, 99)
        a3 <- stringOf(alphaChar)
        n2 <- choose[Int](1, 9)
        n3 <- choose[Int](1, 9)
      } yield AddressPostcode(s"$a1$a2$n1 $a3$n2$n3")
    }
  implicit lazy val arbitraryAdjustmentInformation: Arbitrary[Option[AdjustmentInformation]] =
    Arbitrary {
      None
    }

  implicit lazy val arbitraryChild: Arbitrary[Child] =
    Arbitrary {
      for {
        fullName              <- arbitrary[FullName]
        dateOfBirth           <- arbitrary[LocalDate]
        relationshipStartDate <- arbitrary[LocalDate]
        relationshipEndDate   <- arbitrary[Option[LocalDate]]
      } yield Child(
        fullName,
        dateOfBirth,
        relationshipStartDate,
        relationshipEndDate
      )
    }

  // Claimant Bank Information and associated models
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
        name <- stringOf(alphaChar)
      } yield FirstForename(name)
    }
  implicit lazy val arbitrarySurname: Arbitrary[Surname] =
    Arbitrary {
      for {
        name <- stringOf(alphaChar)
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
  implicit lazy val arbitraryAccountHolderName: Arbitrary[AccountHolderName] =
    Arbitrary {
      for {
        accountHolderName <- alphaStr.suchThat(_.length > 0).map(_.take(ACCOUNT_HOLDER_MAX_LENGTH))
      } yield AccountHolderName(accountHolderName)
    }
  implicit lazy val arbitrarySortCode: Arbitrary[SortCode] =
    Arbitrary {
      for {
        sortCode <- numStr.map(_.take(ALLOWED_SORT_CODE_LENGTH))
      } yield SortCode(sortCode)
    }
  implicit lazy val arbitraryBankAccountNumber: Arbitrary[BankAccountNumber] =
    Arbitrary {
      for {
        accountNumber <- numStr.map(_.take(ALLOWED_ACCOUNT_NUMBER_LENGTH))
      } yield BankAccountNumber(accountNumber)
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

  val generateId: Arbitrary[String] =
    Arbitrary {
      for {
        id <- alphaNumStr.suchThat(_.length >= 25).map(_.take(25))
      } yield id
    }
}
