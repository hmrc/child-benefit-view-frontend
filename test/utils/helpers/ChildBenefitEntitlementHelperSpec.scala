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

package utils.helpers

import generators.ModelGenerators
import models.common.{AddressLine, AddressPostcode}
import models.entitlement.{ChildBenefitEntitlement, FullName}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.Table
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import utils.helpers.ChildBenefitEntitlementHelper.formatChildBenefitEntitlement

// scalastyle:off number.of.methods
class ChildBenefitEntitlementHelperSpec extends AnyFreeSpec with Matchers with ModelGenerators {
  type Transformer = ChildBenefitEntitlement => ChildBenefitEntitlement
  type Selector[A] = ChildBenefitEntitlement => A
  type TestCase[A] = (String, String, Transformer, Selector[A], A)

  // Claimant test case group
  val claimantGroup = "Claimant"
  // Full Name test cases
  val fullName = "Full Name"
  def fullNameLowercase: TestCase[FullName] = generateFullNameTestCase(lowercaseTestName, lowercaseTestValue)
  def fullNameUppercase: TestCase[FullName] = generateFullNameTestCase(uppercaseTestName, uppercaseTestValue)
  def fullNameMixedCase: TestCase[FullName] = generateFullNameTestCase(mixedCaseTestName, mixedCaseTestValue)
  def fullNameExcepted: TestCase[FullName] =
    generateFullNameTestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def generateFullNameTestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[FullName] =
    generateTestCase(
      claimantGroup,
      s"Full Name - $testCaseName",
      testValue,
      expectedResult,
      str => cbe => cbe.copy(claimant = cbe.claimant.copy(name = FullName(str))),
      cbe => cbe.claimant.name,
      str => FullName(str)
    )
  // Address Line 1 test cases
  def addressLine1Lowercase: TestCase[AddressLine] = generateAddressLine1TestCase(lowercaseTestName, lowercaseTestValue)
  def addressLine1Uppercase: TestCase[AddressLine] = generateAddressLine1TestCase(uppercaseTestName, uppercaseTestValue)
  def addressLine1MixedCase: TestCase[AddressLine] = generateAddressLine1TestCase(mixedCaseTestName, mixedCaseTestValue)
  def addressLine1Excepted: TestCase[AddressLine] =
    generateAddressLine1TestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def generateAddressLine1TestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[AddressLine] =
    generateTestCase(
      claimantGroup,
      s"Address Line 1 - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine1 = AddressLine(str)))
          ),
      cbe => cbe.claimant.fullAddress.addressLine1,
      str => AddressLine(str)
    )

  // Address Line 2 test cases
  def addressLine2Lowercase: TestCase[AddressLine] = generateAddressLine2TestCase(lowercaseTestName, lowercaseTestValue)
  def addressLine2Uppercase: TestCase[AddressLine] = generateAddressLine2TestCase(uppercaseTestName, uppercaseTestValue)
  def addressLine2MixedCase: TestCase[AddressLine] = generateAddressLine2TestCase(mixedCaseTestName, mixedCaseTestValue)
  def addressLine2Excepted: TestCase[AddressLine] =
    generateAddressLine2TestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def generateAddressLine2TestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[AddressLine] =
    generateTestCase(
      claimantGroup,
      s"Address Line 2 - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine2 = AddressLine(str)))
          ),
      cbe => cbe.claimant.fullAddress.addressLine2,
      str => AddressLine(str)
    )

  // Address Line 3 test cases
  def addressLine3Lowercase: TestCase[Option[AddressLine]] =
    generateAddressLine3TestCase(lowercaseTestName, lowercaseTestValue)
  def addressLine3Uppercase: TestCase[Option[AddressLine]] =
    generateAddressLine3TestCase(uppercaseTestName, uppercaseTestValue)
  def addressLine3MixedCase: TestCase[Option[AddressLine]] =
    generateAddressLine3TestCase(mixedCaseTestName, mixedCaseTestValue)
  def addressLine3Excepted: TestCase[Option[AddressLine]] =
    generateAddressLine3TestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def addressLine3NoneTestCase(): TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 3 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine3 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine3,
      None: Option[AddressLine]
    )
  def generateAddressLine3TestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[Option[AddressLine]] =
    generateTestCase(
      claimantGroup,
      s"Address Line 3 - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine3 = Some(AddressLine(str))))
          ),
      cbe => cbe.claimant.fullAddress.addressLine3,
      str => Some(AddressLine(str))
    )

  // Address Line 4 test cases
  def addressLine4Lowercase: TestCase[Option[AddressLine]] =
    generateAddressLine4TestCase(lowercaseTestName, lowercaseTestValue)
  def addressLine4Uppercase: TestCase[Option[AddressLine]] =
    generateAddressLine4TestCase(uppercaseTestName, uppercaseTestValue)
  def addressLine4MixedCase: TestCase[Option[AddressLine]] =
    generateAddressLine4TestCase(mixedCaseTestName, mixedCaseTestValue)
  def addressLine4Excepted: TestCase[Option[AddressLine]] =
    generateAddressLine4TestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def addressLine4NoneTestCase(): TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 4 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine4 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine4,
      None: Option[AddressLine]
    )
  def generateAddressLine4TestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[Option[AddressLine]] =
    generateTestCase(
      claimantGroup,
      s"Address Line 4 - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine4 = Some(AddressLine(str))))
          ),
      cbe => cbe.claimant.fullAddress.addressLine4,
      str => Some(AddressLine(str))
    )

  // Address Line 5 test cases
  def addressLine5Lowercase: TestCase[Option[AddressLine]] =
    generateAddressLine5TestCase(lowercaseTestName, lowercaseTestValue)
  def addressLine5Uppercase: TestCase[Option[AddressLine]] =
    generateAddressLine5TestCase(uppercaseTestName, uppercaseTestValue)
  def addressLine5MixedCase: TestCase[Option[AddressLine]] =
    generateAddressLine5TestCase(mixedCaseTestName, mixedCaseTestValue)
  def addressLine5Excepted: TestCase[Option[AddressLine]] =
    generateAddressLine5TestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)
  def addressLine5NoneTestCase(): TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 5 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine5 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine5,
      None: Option[AddressLine]
    )
  def generateAddressLine5TestCase(
      testCaseName:   String,
      testValue:      String,
      expectedResult: String = defaultExpectedResult
  ): TestCase[Option[AddressLine]] =
    generateTestCase(
      claimantGroup,
      s"Address Line 5 - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine5 = Some(AddressLine(str))))
          ),
      cbe => cbe.claimant.fullAddress.addressLine5,
      str => Some(AddressLine(str))
    )

  // Address Post Code test cases
  def addressPostcodeLowercase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(lowercaseTestName, "sw1a 2bq")
  def addressPostcodeUppercase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(uppercaseTestName, "SW1A 2BQ")
  def addressPostcodeMixedCase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(mixedCaseTestName, "sW1a 2bQ")
  def generateAddressPostcodeTestCase(testCaseName: String, testValue: String): TestCase[AddressPostcode] =
    generateTestCase(
      claimantGroup,
      s"Address Postcode - $testCaseName",
      testValue,
      "SW1A 2BQ",
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressPostcode = AddressPostcode(str)))
          ),
      cbe => cbe.claimant.fullAddress.addressPostcode,
      str => AddressPostcode(str)
    )

  //Base Test Case work
  val lowercaseTestName      = "lowercase"
  val lowercaseTestValue     = "test value"
  val uppercaseTestName      = "uppercase"
  val uppercaseTestValue     = "TEST VALUE"
  val mixedCaseTestName      = "mixed case"
  val mixedCaseTestValue     = "TesT VaLuE"
  val defaultExpectedResult  = "Test Value"
  val exceptedCaseTestName   = "excepted words"
  val exceptedCaseTestValue  = "Test and Value"
  val exceptedExpectedResult = "Test and Value"

  def generateTestCase[A](
      groupName:      String,
      testCaseName:   String,
      testValue:      String,
      expectedValue:  String,
      testSetter:     String => Transformer,
      selector:       Selector[A],
      expectedSetter: String => A
  ): TestCase[A] =
    (
      groupName,
      testCaseName,
      testSetter(testValue),
      selector,
      expectedSetter(expectedValue)
    )

  "formatChildBenefitEntitlement" - {
    val testCases = Table(
      ("groupName", "caseName", "transform", "propertySelector", "expectedValue"),
      fullNameLowercase,
      fullNameUppercase,
      fullNameMixedCase,
      fullNameExcepted,
      addressLine1Lowercase,
      addressLine1Uppercase,
      addressLine1MixedCase,
      addressLine1Excepted,
      addressLine2Lowercase,
      addressLine2Uppercase,
      addressLine2MixedCase,
      addressLine2Excepted,
      addressLine3Lowercase,
      addressLine3Uppercase,
      addressLine3MixedCase,
      addressLine3Excepted,
      addressLine3NoneTestCase,
      addressLine4Lowercase,
      addressLine4Uppercase,
      addressLine4MixedCase,
      addressLine4Excepted,
      addressLine4NoneTestCase,
      addressLine5Lowercase,
      addressLine5Uppercase,
      addressLine5MixedCase,
      addressLine5Excepted,
      addressLine5NoneTestCase,
      addressPostcodeLowercase,
      addressPostcodeUppercase,
      addressPostcodeMixedCase
    )
    "should return a CBE with the expected field formatted" - {
      forAll(testCases) {
        (groupName: String, caseName: String, transform: Transformer, propertySelector, expectedValue) =>
          s"$groupName: $caseName" in {
            forAll(arbitrary[ChildBenefitEntitlement] -> "cbe") { cbe =>
              val transformedCBE = transform(cbe)
              val sutResult      = formatChildBenefitEntitlement(transformedCBE)
              propertySelector(sutResult) mustEqual expectedValue
            }
          }
      }
    }
  }
}
