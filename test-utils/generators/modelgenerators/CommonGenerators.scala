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
import models.common._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.alphaStr

trait CommonGenerators extends DataGenerators {
  implicit lazy val arbitraryAddressLine: Arbitrary[AddressLine] =
    Arbitrary {
      for {
        line <- generateAddressLine
      } yield AddressLine(line)
    }
  implicit lazy val arbitraryPostcode: Arbitrary[AddressPostcode] =
    Arbitrary {
      for {
        postcode <- generatePostCode
      } yield AddressPostcode(postcode)
    }
  implicit lazy val arbitraryAdjustmentReasonCode: Arbitrary[AdjustmentReasonCode] =
    Arbitrary {
      for {
        reason <- alphaStr
      } yield AdjustmentReasonCode(reason)
    }
  implicit lazy val arbitraryChildReferenceNumber: Arbitrary[ChildReferenceNumber] =
    Arbitrary {
      for {
        referenceNumber <- generateReferenceNumber
      } yield ChildReferenceNumber(referenceNumber)
    }
  implicit lazy val arbitraryFirstForename: Arbitrary[FirstForename] = {
    Arbitrary {
      for {
        name <- generateName
      } yield FirstForename(name)
    }
  }
  implicit lazy val arbitraryNationalInsuranceNumber: Arbitrary[NationalInsuranceNumber] = {
    Arbitrary {
      for {
        nino <- generateNino
      } yield NationalInsuranceNumber(nino)
    }
  }
  implicit lazy val arbitrarySurname: Arbitrary[Surname] =
    Arbitrary {
      for {
        name <- generateName
      } yield Surname(name)
    }
}
