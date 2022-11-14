/*
 * Copyright 2022 HM Revenue & Customs
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

import models.cob.{ConfirmNewAccountDetails, NewAccountDetails}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Arbitrary

trait ModelGenerators {
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
}
