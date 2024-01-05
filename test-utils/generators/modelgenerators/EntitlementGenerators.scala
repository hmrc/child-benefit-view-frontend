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
import models.common.{AddressLine, AddressPostcode, AdjustmentReasonCode}
import models.entitlement._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait EntitlementGenerators extends DataGenerators with CommonGenerators {
  implicit lazy val arbitraryChildBenefitEntitlement: Arbitrary[ChildBenefitEntitlement] =
    Arbitrary {
      for {
        claimant                         <- arbitrary[Claimant]
        entitlementDate                  <- arbitrary[LocalDate]
        paidAmountForEldestOrOnlyChild   <- arbitrary[Double]
        paidAmountForEachAdditionalChild <- arbitrary[Double]
        children                         <- Gen.containerOf[List, Child](arbitrary(arbitraryChild)) suchThat (x => x.nonEmpty)
      } yield ChildBenefitEntitlement(
        claimant,
        entitlementDate,
        BigDecimal(paidAmountForEldestOrOnlyChild),
        BigDecimal(paidAmountForEachAdditionalChild),
        children
      )
    }
  implicit lazy val arbitraryClaimant: Arbitrary[Claimant] =
    Arbitrary {
      for {
        fullName              <- arbitrary[FullName]
        awardValue            <- arbitrary[Double]
        awardStartDate        <- arbitrary[LocalDate]
        awardEndDate          <- arbitrary[LocalDate]
        higherRateValue       <- arbitrary[Double]
        standardRateValue     <- arbitrary[Double]
        lastPaymentInfo       <- Gen.containerOf[List, LastPaymentFinancialInfo](arbitrary(arbitraryLastPaymentFinancialInfo))
        fullAddress           <- arbitrary[FullAddress]
        adjustmentInformation <- arbitrary[Option[AdjustmentInformation]]
      } yield Claimant(
        fullName,
        BigDecimal(awardValue),
        awardStartDate,
        awardEndDate,
        BigDecimal(higherRateValue),
        BigDecimal(standardRateValue),
        lastPaymentInfo,
        fullAddress,
        adjustmentInformation
      )
    }
  implicit lazy val arbitraryFullName: Arbitrary[FullName] =
    Arbitrary {
      for {
        givenName <- generateName
        surname   <- generateName
      } yield FullName(s"$givenName $surname")
    }
  implicit lazy val arbitraryLastPaymentFinancialInfo: Arbitrary[LastPaymentFinancialInfo] =
    Arbitrary {
      for {
        creditDate   <- arbitrary[LocalDate]
        creditAmount <- arbitrary[Double]
      } yield LastPaymentFinancialInfo(creditDate, BigDecimal(creditAmount))
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
  implicit lazy val arbitraryNoAdjustmentInformation: Arbitrary[Option[AdjustmentInformation]] =
    Arbitrary {
      None
    }

  implicit lazy val arbitraryChild: Arbitrary[Child] =
    Arbitrary {
      for {
        name          <- arbitrary[FullName]
        dateOfBirth   <- arbitrary[LocalDate]
        relationStart <- arbitrary[LocalDate]
        relationEnd   <- arbitrary[Option[LocalDate]]
      } yield Child(name, dateOfBirth, relationStart, relationEnd)
    }

  implicit lazy val arbitraryAdjustmentInformation: Arbitrary[AdjustmentInformation] = {
    Arbitrary {
      for {
        code    <- arbitrary[AdjustmentReasonCode]
        endDate <- arbitrary[LocalDate]
      } yield AdjustmentInformation(code, endDate)
    }
  }
}
