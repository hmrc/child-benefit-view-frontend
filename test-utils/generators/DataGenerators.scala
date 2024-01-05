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

package generators

import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaChar, alphaNumStr, alphaStr, choose, numStr}

trait DataGenerators {
  private val MAX_ID_LENGTH = 20
  val generateId: Gen[String] = alphaNumStr.map(_.take(MAX_ID_LENGTH))

  private val NINO_NUMERIC_LENGTH = 6
  val generateNino: Gen[String] = for {
    pre1    <- alphaChar
    pre2    <- alphaChar
    numeric <- numStr.suchThat(_.length >= NINO_NUMERIC_LENGTH)
    suffix  <- alphaChar
  } yield s"${pre1.toUpper}${pre2.toUpper}${numeric.take(NINO_NUMERIC_LENGTH)}${suffix.toUpper}"
  val generateReferenceNumber: Gen[String] = for {
    pre1    <- alphaChar
    pre2    <- alphaChar
    numeric <- numStr.suchThat(_.length >= NINO_NUMERIC_LENGTH)
  } yield s"${pre1.toUpper}${pre2.toUpper}${numeric.take(NINO_NUMERIC_LENGTH)}"

  private val MAX_NAME_LENGTH = 30
  val generateName: Gen[String] = alphaStr.map(_.take(MAX_NAME_LENGTH))

  private val SORT_CODE_LENGTH = 6
  val generateSortCode: Gen[String] = numStr.suchThat(_.length >= SORT_CODE_LENGTH).map(_.take(SORT_CODE_LENGTH))

  private val ACCOUNT_NUMBER_CODE_LENGTH = 8
  val generateAccountNumber: Gen[String] =
    numStr.suchThat(_.length >= ACCOUNT_NUMBER_CODE_LENGTH).map(_.take(ACCOUNT_NUMBER_CODE_LENGTH))

  private val MIN_BUILDING_SOCIETY_NUMBER_CODE_LENGTH = 1
  private val MAX_BUILDING_SOCIETY_NUMBER_CODE_LENGTH = 18
  val generateBuildingSocietyNumber: Gen[String] =
    alphaNumStr
      .suchThat(_.length >= MIN_BUILDING_SOCIETY_NUMBER_CODE_LENGTH)
      .map(_.take(MAX_BUILDING_SOCIETY_NUMBER_CODE_LENGTH))

  private val MIN_ADDRESS_LINE_LENGTH = 15
  private val MAX_ADDRESS_LINE_LENGTH = 30
  val generateAddressLine: Gen[String] =
    alphaStr.suchThat(_.length >= MIN_ADDRESS_LINE_LENGTH).map(_.take(MAX_ADDRESS_LINE_LENGTH))

  val generatePostCode: Gen[String] =
    for {
      a1 <- alphaChar
      a2 <- alphaChar
      n1 <- choose[Int](1, 99)
      a3 <- alphaChar
      n2 <- choose[Int](1, 9)
      n3 <- choose[Int](1, 9)
    } yield s"$a1$a2$n1 $a3$n2$n3"
}
