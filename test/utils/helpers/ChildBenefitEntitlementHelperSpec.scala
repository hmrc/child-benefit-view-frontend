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
import models.entitlement.{ChildBenefitEntitlement, FullAddress, FullName}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.Table
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import utils.helpers.ChildBenefitEntitlementHelper.formatChildBenefitEntitlement
import utils.helpers.ChildBenefitEntitlementHelperSpec._

class ChildBenefitEntitlementHelperSpec extends AnyFreeSpec with Matchers with ModelGenerators {
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
object ChildBenefitEntitlementHelperSpec {

  type Transformer = ChildBenefitEntitlement => ChildBenefitEntitlement
  type Selector[A] = ChildBenefitEntitlement => A
  type TestCase[A] = (String, String, Transformer, Selector[A], A)

  //Base Test Case work
  private val lowercaseTestName = "lowercase"
  private val lowercaseTestValue = "test value"
  private val uppercaseTestName = "uppercase"
  private val uppercaseTestValue = "TEST VALUE"
  private val mixedCaseTestName = "mixed case"
  private val mixedCaseTestValue = "TesT VaLuE"
  private val defaultExpectedResult = "Test Value"
  private val exceptedCaseTestName = "excepted words"
  private val exceptedCaseTestValue = "Test and Value"
  private val exceptedExpectedResult = "Test and Value"

  private def generateTestCase[A](
                                   groupName: String,
                                   testCaseName: String,
                                   testValue: String,
                                   expectedValue: String,
                                   testSetter: String => Transformer,
                                   selector: Selector[A],
                                   expectedSetter: String => A
                                 ): TestCase[A] =
    (
      groupName,
      testCaseName,
      testSetter(testValue),
      selector,
      expectedSetter(expectedValue)
    )

  // Claimant test case group
  private val claimantGroup = "Claimant"

  // Full Name test cases
  val fullNameLowercase: TestCase[FullName] = generateFullNameTestCase(lowercaseTestName, lowercaseTestValue)
  val fullNameUppercase: TestCase[FullName] = generateFullNameTestCase(uppercaseTestName, uppercaseTestValue)
  val fullNameMixedCase: TestCase[FullName] = generateFullNameTestCase(mixedCaseTestName, mixedCaseTestValue)
  val fullNameExcepted: TestCase[FullName] =
    generateFullNameTestCase(exceptedCaseTestName, exceptedCaseTestValue, exceptedExpectedResult)

  private def generateFullNameTestCase(
                                        testCaseName: String,
                                        testValue: String,
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
  val addressLine1Lowercase: TestCase[AddressLine] = generateAddressLineTestCase(lowercaseTestName, lowercaseTestValue, "1")
  val addressLine1Uppercase: TestCase[AddressLine] = generateAddressLineTestCase(uppercaseTestName, uppercaseTestValue, "1")
  val addressLine1MixedCase: TestCase[AddressLine] = generateAddressLineTestCase(mixedCaseTestName, mixedCaseTestValue, "1")
  val addressLine1Excepted: TestCase[AddressLine] =
    generateAddressLineTestCase(exceptedCaseTestName, exceptedCaseTestValue, "1", exceptedExpectedResult)

  // Address Line 2 test cases
  val addressLine2Lowercase: TestCase[AddressLine] = generateAddressLineTestCase(lowercaseTestName, lowercaseTestValue, "2")
  val addressLine2Uppercase: TestCase[AddressLine] = generateAddressLineTestCase(uppercaseTestName, uppercaseTestValue, "2")
  val addressLine2MixedCase: TestCase[AddressLine] = generateAddressLineTestCase(mixedCaseTestName, mixedCaseTestValue, "2")
  val addressLine2Excepted: TestCase[AddressLine] =
    generateAddressLineTestCase(exceptedCaseTestName, exceptedCaseTestValue, "2", exceptedExpectedResult)

  // Address Line 3 test cases
  val addressLine3Lowercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(lowercaseTestName, lowercaseTestValue, "3")
  val addressLine3Uppercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(uppercaseTestName, uppercaseTestValue, "3")
  val addressLine3MixedCase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(mixedCaseTestName, mixedCaseTestValue, "3")
  val addressLine3Excepted: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(exceptedCaseTestName, exceptedCaseTestValue, "3", exceptedExpectedResult)
  val addressLine3NoneTestCase: TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 3 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine3 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine3,
      None: Option[AddressLine]
    )

  // Address Line 4 test cases
  val addressLine4Lowercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(lowercaseTestName, lowercaseTestValue, "4")
  val addressLine4Uppercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(uppercaseTestName, uppercaseTestValue, "4")
  val addressLine4MixedCase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(mixedCaseTestName, mixedCaseTestValue, "4")
  val addressLine4Excepted: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(exceptedCaseTestName, exceptedCaseTestValue, "4", exceptedExpectedResult)
  val addressLine4NoneTestCase: TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 4 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine4 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine4,
      None: Option[AddressLine]
    )

  // Address Line 5 test cases
  val addressLine5Lowercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(lowercaseTestName, lowercaseTestValue, "5")
  val addressLine5Uppercase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(uppercaseTestName, uppercaseTestValue, "5")
  val addressLine5MixedCase: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(mixedCaseTestName, mixedCaseTestValue, "5")
  val addressLine5Excepted: TestCase[Option[AddressLine]] =
    generateOptionalAddressLineTestCase(exceptedCaseTestName, exceptedCaseTestValue, "5", exceptedExpectedResult)
  val addressLine5NoneTestCase: TestCase[Option[AddressLine]] =
    (
      claimantGroup,
      s"Address Line 5 - None",
      (cbe: ChildBenefitEntitlement) =>
        cbe.copy(claimant = cbe.claimant.copy(fullAddress = cbe.claimant.fullAddress.copy(addressLine5 = None))),
      (cbe: ChildBenefitEntitlement) => cbe.claimant.fullAddress.addressLine5,
      None: Option[AddressLine]
    )

  private def generateAddressLineTestCase(
                                           testCaseName: String,
                                           testValue: String,
                                           line: String,
                                           expectedResult: String = defaultExpectedResult
                                         ): TestCase[AddressLine] =
    generateTestCase(
      claimantGroup,
      s"Address Line $line - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = getAddressLineCopy(cbe.claimant.fullAddress, str, line))
          ),
      getAddressSelector(line),
      str => AddressLine(str)
    )

  private def generateOptionalAddressLineTestCase(
                                                   testCaseName: String,
                                                   testValue: String,
                                                   line: String,
                                                   expectedResult: String = defaultExpectedResult
                                                 ): TestCase[Option[AddressLine]] =
    generateTestCase(
      claimantGroup,
      s"Address Line $line - $testCaseName",
      testValue,
      expectedResult,
      str =>
        cbe =>
          cbe.copy(claimant =
            cbe.claimant.copy(fullAddress = getAddressLineCopy(cbe.claimant.fullAddress, str, line))
          ),
      getOptionalAddressSelector(line),
      str => Some(AddressLine(str))
    )

  private def getAddressLineCopy(address: FullAddress, str: String, line: String): FullAddress =
    line match {
      case "1" => address.copy(addressLine1 = AddressLine(str))
      case "2" => address.copy(addressLine2 = AddressLine(str))
      case "3" => address.copy(addressLine3 = Some(AddressLine(str)))
      case "4" => address.copy(addressLine4 = Some(AddressLine(str)))
      case "5" => address.copy(addressLine5 = Some(AddressLine(str)))
    }

  private def getAddressSelector(line: String): Selector[AddressLine] =
    line match {
      case "1" => cbe => cbe.claimant.fullAddress.addressLine1
      case "2" => cbe => cbe.claimant.fullAddress.addressLine2
    }

  private def getOptionalAddressSelector(line: String): Selector[Option[AddressLine]] =
    line match {
      case "3" => cbe => cbe.claimant.fullAddress.addressLine3
      case "4" => cbe => cbe.claimant.fullAddress.addressLine4
      case "5" => cbe => cbe.claimant.fullAddress.addressLine5
    }

  // Address Post Code test cases
  val addressPostcodeLowercase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(lowercaseTestName, "sw1a 2bq")
  val addressPostcodeUppercase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(uppercaseTestName, "SW1A 2BQ")
  val addressPostcodeMixedCase: TestCase[AddressPostcode] =
    generateAddressPostcodeTestCase(mixedCaseTestName, "sW1a 2bQ")

  private def generateAddressPostcodeTestCase(testCaseName: String, testValue: String): TestCase[AddressPostcode] =
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
}
