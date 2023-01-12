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

package utils.helpers

import generators.ModelGenerators
import models.changeofbank.ClaimantBankInformation
import models.common.{FirstForename, Surname}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.Table
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import utils.helpers.ClaimantBankInformationHelper.formatClaimantBankInformation
import utils.helpers.ClaimantBankInformationHelperSpec._

class ClaimantBankInformationHelperSpec extends AnyFreeSpec with Matchers with OptionValues with ModelGenerators {
  "formatChildBenefitEntitlement" - {
    val testCases = Table(
      ("caseName", "transform", "propertySelector", "expectedValue"),
      firstForenameLowercase,
      firstForenameUppercase,
      firstForenameMixedCase,
      firstForenameExcepted,
      surnameLowercase,
      surnameUppercase,
      surnameMixedCase,
      surnameExcepted
    )
    "should return a CBI with the expected field formatted" - {
      forAll(testCases) { (caseName: String, transform: Transformer, propertySelector, expectedValue) =>
        s"$caseName" in {
          forAll(arbitrary[ClaimantBankInformation] -> "cbi") { cbi =>
            val transformedCBI = transform(cbi)
            val sutResult      = formatClaimantBankInformation(transformedCBI)
            propertySelector(sutResult) mustEqual expectedValue
          }
        }
      }
    }
  }
}

object ClaimantBankInformationHelperSpec {
  type Transformer = ClaimantBankInformation => ClaimantBankInformation
  type Selector[A] = ClaimantBankInformation => A
  type TestCase[A] = (String, Transformer, Selector[A], A)

  //Base Test Case work
  private val lowercaseTestName      = "lowercase"
  private val lowercaseTestValue     = "test value"
  private val uppercaseTestName      = "uppercase"
  private val uppercaseTestValue     = "TEST VALUE"
  private val mixedCaseTestName      = "mixed case"
  private val mixedCaseTestValue     = "TesT VaLuE"
  private val defaultExpectedResult  = "Test Value"
  private val exceptedCaseTestName   = "excepted words"
  private val exceptedCaseTestValue  = "Test and Value"
  private val exceptedExpectedResult = "Test and Value"

  private def generateTestCase[A](
      testCaseName:   String,
      testValue:      String,
      expectedValue:  String,
      testSetter:     String => Transformer,
      selector:       Selector[A],
      expectedSetter: String => A
  ): TestCase[A] =
    (
      testCaseName,
      testSetter(testValue),
      selector,
      expectedSetter(expectedValue)
    )

  // First Forename test cases
  val firstForenameLowercase: TestCase[FirstForename] =
    generateFirstForenameTestCase(lowercaseTestName, lowercaseTestValue)
  val firstForenameUppercase: TestCase[FirstForename] =
    generateFirstForenameTestCase(uppercaseTestName, uppercaseTestValue)
  val firstForenameMixedCase: TestCase[FirstForename] =
    generateFirstForenameTestCase(mixedCaseTestName, mixedCaseTestValue)
  val firstForenameExcepted: TestCase[FirstForename] =
    generateFirstForenameTestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)

  private def generateFirstForenameTestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[FirstForename] =
    generateTestCase(
      s"First Forename - $testCaseName",
      testValue,
      expectedResult,
      str => cbi => cbi.copy(firstForename = FirstForename(str)),
      cbi => cbi.firstForename,
      str => FirstForename(str)
    )

  // Surname test cases
  val surnameLowercase: TestCase[Surname] = generateSurnameTestCase(lowercaseTestName, lowercaseTestValue)
  val surnameUppercase: TestCase[Surname] = generateSurnameTestCase(uppercaseTestName, uppercaseTestValue)
  val surnameMixedCase: TestCase[Surname] = generateSurnameTestCase(mixedCaseTestName, mixedCaseTestValue)
  val surnameExcepted: TestCase[Surname] =
    generateSurnameTestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)

  private def generateSurnameTestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[Surname] =
    generateTestCase(
      s"Surname - $testCaseName",
      testValue,
      expectedResult,
      str => cbi => cbi.copy(surname = Surname(str)),
      cbi => cbi.surname,
      str => Surname(str)
    )
}
