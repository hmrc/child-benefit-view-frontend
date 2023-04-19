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

import models.changeofbank.{ClaimantBankAccountInformation, ClaimantBankInformation, ClaimantFinancialDetails}
import models.cob.{ConfirmNewAccountDetails, NewAccountDetails}
import models.common.{AddressLine, AddressPostcode, FirstForename, NationalInsuranceNumber, Surname}
import models.entitlement._
import models.ftnae.HowManyYears
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryHowManyYears: Arbitrary[HowManyYears] =
    Arbitrary {
      Gen.oneOf(HowManyYears.values.toSeq)
    }
  private val ALLOWED_SORT_CODE_LENGTH = 6
  implicit lazy val arbitraryNewAccountDetails: Arbitrary[NewAccountDetails] =
    Arbitrary {
      for {
        accountHolder <- arbitrary[String]
        sortCode      <- arbitrary[String].map(_.take(ALLOWED_SORT_CODE_LENGTH))
        accountNumber <- arbitrary[String]
      } yield NewAccountDetails(accountHolder, sortCode, accountNumber)
    }
  implicit lazy val arbitraryConfirmNewAccountDetails: Arbitrary[ConfirmNewAccountDetails] =
    Arbitrary {
      for {
        index <- arbitrary[Int].map(Math.abs(_))
      } yield ConfirmNewAccountDetails.values(index % ConfirmNewAccountDetails.values.length)
    }

  // Child Benefit Entitlements and associated model
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
  implicit lazy val arbitraryNationalInsuranceNumber: Arbitrary[NationalInsuranceNumber] =
    Arbitrary {
      arbitrary[String].map(NationalInsuranceNumber(_))
    }

  implicit lazy val arbitraryNinoSuffix: Arbitrary[NinoSuffix] =
    Arbitrary {
      arbitrary[String].map(NinoSuffix(_))
    }

  implicit lazy val arbitraryChild: Arbitrary[Child] =
    Arbitrary {
      for {
        fullName                <- arbitrary[FullName]
        dateOfBirth             <- arbitrary[LocalDate]
        relationshipStartDate   <- arbitrary[LocalDate]
        relationshipEndDate     <- arbitrary[Option[LocalDate]]
        nationalInsuranceNumber <- arbitrary[Option[NationalInsuranceNumber]]
        ninoSuffix              <- arbitrary[Option[NinoSuffix]]
        crnIndicator            <- arbitrary[Option[Int]]
      } yield Child(
        fullName,
        dateOfBirth,
        relationshipStartDate,
        relationshipEndDate,
        nationalInsuranceNumber,
        ninoSuffix,
        crnIndicator
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
        name <- arbitrary[String]
      } yield FirstForename(name)
    }
  implicit lazy val arbitrarySurname: Arbitrary[Surname] =
    Arbitrary {
      for {
        name <- arbitrary[String]
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

}
