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

package config

import base.BaseSpec
import org.mockito.Mockito.when
import org.scalatest.Assertion
import play.api.i18n.Lang
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration

class FrontendAppConfigSpec extends BaseSpec {
  "FrontendAppConfig" - {
    val mockConfiguration  = mock[Configuration]
    val mockServicesConfig = mock[ServicesConfig]

    val sut = new FrontendAppConfig(mockConfiguration, mockServicesConfig)

    def mustEqual[T]: (String, (T, T, Option[T]) => Assertion) =
      (
        "must equal",
        (result: T, expectedValue: T, _: Option[T]) => {
          result mustBe expectedValue
        }
      )
    def mustInclude(expectBaseValue: Boolean = true): (String, (String, String, Option[String]) => Assertion) =
      (
        "must include",
        (result: String, expectedValue: String, baseValue: Option[String]) => {
          if (expectBaseValue) {
            baseValue.map(v => result must include(v))
          }
          result must include(expectedValue)
        }
      )

    val baseServiceKey   = "child-benefit-entitlement"
    val baseServiceValue = "http://unit-test.service"
    val baseExitKey      = "microservice.services.feedback-frontend.url"
    val baseExitValue    = "http://unit-test.exit"
    val baseContactKey   = "contact-frontend.host"
    val baseContactValue = "http://unit-test.contact"
    val referrerUrl      = "referrer.url"

    def generateConfigTestCase[T](
        name:          String,
        setup:         (Configuration, ServicesConfig) => T,
        valueToSet:    Option[T],
        expectedValue: T,
        getResult:     FrontendAppConfig => T,
        testSuite:     (String, (T, T, Option[T]) => Assertion)
    ) = (name, setup, valueToSet, expectedValue, getResult, testSuite)

    def configSetup[T](key: String)(implicit loader: ConfigLoader[T]): (Configuration, ServicesConfig) => T =
      (config: Configuration, _: ServicesConfig) => config.get[T](key)
    def serviceConfigSetup(key: String): (Configuration, ServicesConfig) => String =
      (_: Configuration, servicesConfig: ServicesConfig) => servicesConfig.baseUrl(key)

    def getResult[T](getResult: FrontendAppConfig => T): FrontendAppConfig => T = getResult

    val stringTestCases = Table(
      ("testName", "setup", "valueToSet", "expectedValue", "getResult", "testSuite"),
      generateConfigTestCase(
        "Host",
        configSetup[String]("host"),
        None,
        "http://unit-test.host.com",
        getResult(_.host),
        mustEqual
      ),
      generateConfigTestCase(
        "App Name",
        configSetup[String]("appName"),
        None,
        "unitTestApp",
        getResult(_.appName),
        mustEqual
      ),
      generateConfigTestCase(
        "Login Url",
        configSetup[String]("urls.login"),
        None,
        "http://login.unit-test.com",
        getResult(_.loginUrl),
        mustEqual
      ),
      generateConfigTestCase(
        "Sign Out Url",
        configSetup[String]("urls.signOut"),
        None,
        "http://signout.unit-test.com",
        getResult(_.signOutUrl),
        mustEqual
      ),
      generateConfigTestCase(
        "Uplift Url",
        configSetup[String]("urls.ivUplift"),
        None,
        "http://uplift.unit-test.com",
        getResult(_.ivUpliftUrl),
        mustEqual
      ),
      generateConfigTestCase(
        "Child Benefit Entitlement Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/view-entitlements-and-payments",
        getResult(_.childBenefitEntitlementUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Change of Bank Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/change-bank-user-information",
        getResult(_.changeOfBankUserInfoUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Get FTNAE Account Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/retrieve-ftnae-account-information",
        getResult(_.getFtnaeAccountInfoUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Update FTNAE Info Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/update-child-information/ftnae",
        getResult(_.updateFtnaeInfoUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Verify Bank Account Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/verify-claimant-bank-account",
        getResult(_.verifyBankAccountUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Verify BAR Not Locked Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/verify-bar-not-locked",
        getResult(_.verifyBARNotLockedUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Update Bank Account Url",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/update-claimant-bank-account",
        getResult(_.updateBankAccountUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Drop Change of Bank Cache",
        serviceConfigSetup(baseServiceKey),
        Some(baseServiceValue),
        "/child-benefit-service/drop-change-bank-cache",
        getResult(_.dropChangeOfBankCacheUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Exit Survey Url",
        configSetup[String](baseExitKey),
        Some(baseExitValue),
        "/feedback/CHIB",
        getResult(_.exitSurveyUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Feedback Url",
        configSetup[String](baseContactKey),
        Some(baseContactValue),
        "/contact/beta-feedback?service=CHIB",
        getResult(_.feedbackUrl),
        mustInclude()
      ),
      generateConfigTestCase(
        "Report Technical Problem Url",
        configSetup[String](baseContactKey),
        Some(baseContactValue),
        s"/contact/report-technical-problem?newTab=true&service=CHIB&referrerUrl=${RedirectUrl(baseContactValue + referrerUrl).get(UnsafePermitAll).encodedUrl}",
        getResult(_.reportTechnicalProblemUrl(referrerUrl)),
        mustInclude()
      )
    )
    val intTestCases = Table(
      ("testName", "setup", "valueToSet", "expectedValue", "getResult", "testSuite"),
      generateConfigTestCase(
        "Dialog Timeout",
        configSetup[Int]("timeout-dialog.timeout"),
        None,
        123,
        getResult(_.timeout),
        mustEqual
      ),
      generateConfigTestCase(
        "Dialog Countdown",
        configSetup[Int]("timeout-dialog.countdown"),
        None,
        456,
        getResult(_.countdown),
        mustEqual
      ),
      generateConfigTestCase(
        "Cache TtL",
        configSetup[Int]("mongodb.timeToLiveInSeconds"),
        None,
        789,
        getResult(_.cacheTtl),
        mustEqual
      )
    )
    val booleanTestCases = Table(
      ("testName", "setup", "valueToSet", "expectedValue", "getResult", "testSuite"),
      generateConfigTestCase(
        "Language Translation Enabled",
        configSetup[Boolean]("features.welsh-translation"),
        None,
        true,
        getResult(_.languageTranslationEnabled),
        mustEqual
      ),
      generateConfigTestCase(
        "Show Outage Banner",
        configSetup[Boolean]("features.showOutageBanner"),
        None,
        false,
        getResult(_.showOutageBanner),
        mustEqual
      ),
      generateConfigTestCase(
        "SCA Wrapper Enabled",
        configSetup[Boolean]("features.sca-wrapper-enabled"),
        None,
        true,
        getResult(_.scaWrapperEnabled),
        mustEqual
      )
    )
    val mapTestCases = Table(
      ("testName", "setup", "valueToSet", "expectedValue", "getResult", "testSuite"),
      generateConfigTestCase(
        "Language Map",
        configSetup[String]("none"),
        None,
        Map("en" -> Lang("en"), "cy" -> Lang("cy")),
        getResult(_.languageMap),
        mustEqual
      )
    )
    "GIVEN the configuration returns a value for the required key" - {
      "THEN FrontendApp Config return a value that satisfies the provided test suite" - {
        forAll(stringTestCases) { (testName, setup, valueToSet, expectedValue, getResult, testSuite) =>
          configurationTest(testName, setup, valueToSet, expectedValue, getResult, testSuite)
        }
        forAll(intTestCases) { (testName, setup, valueToSet, expectedValue, getResult, testSuite) =>
          configurationTest(testName, setup, valueToSet, expectedValue, getResult, testSuite)
        }
        forAll(booleanTestCases) { (testName, setup, valueToSet, expectedValue, getResult, testSuite) =>
          configurationTest(testName, setup, valueToSet, expectedValue, getResult, testSuite)
        }
        forAll(mapTestCases) { (testName, setup, valueToSet, expectedValue, getResult, testSuite) =>
          configurationTest(testName, setup, valueToSet, expectedValue, getResult, testSuite)
        }
      }
    }
    def configurationTest[T](
        testName:      String,
        setup:         (Configuration, ServicesConfig) => T,
        valueToSet:    Option[T],
        expectedValue: T,
        getResult:     FrontendAppConfig => T,
        testSuite:     (String, (T, T, Option[T]) => Assertion)
    ): Unit = {
      when(setup(mockConfiguration, mockServicesConfig)).thenReturn(valueToSet.getOrElse(expectedValue))

      val result                         = getResult(sut)
      val (testSuiteName, testSuiteFunc) = (testSuite._1, testSuite._2)
      s"$testName using test suite: $testSuiteName" in {
        testSuiteFunc(result, expectedValue, valueToSet)
      }
    }

    "Non-standard test cases" - {
      "Confidence Level" - {
        forAll(
          Table(
            ("configValue", "confidenceLevel"),
            (50, ConfidenceLevel.L50),
            (200, ConfidenceLevel.L200),
            (250, ConfidenceLevel.L250)
          )
        ) { (configValue, confidenceLevel) =>
          s"GIVEN a valid Int value is set for confidenceLevel: $configValue" - {
            s"THEN Confidence Level $confidenceLevel is returned" in {
              when(mockConfiguration.get[Int]("confidenceLevel")).thenReturn(configValue)

              val result = sut.confidenceLevel

              result mustBe confidenceLevel
            }
          }
        }
      }
      "ignoreHicbcCacheTTL Duration" - {
        forAll(
          Table(
            ("duration", "timeunit"),
            (20L, "second"),
            (1L, "minute"),
            (2L, "hour")
          )
        ) { (duration, timeunit) =>
          s"GIVEN valid Long value duration: $duration AND a valid String value timeunit: $timeunit are set" - {
            s"THEN Duration ${Duration(duration, timeunit)} is returned" in {
              when(mockConfiguration.get[Long]("features.ignore-hicbc-check-cache.duration")).thenReturn(duration)
              when(mockConfiguration.get[String]("features.ignore-hicbc-check-cache.timeunit")).thenReturn(timeunit)

              val result = sut.ignoreHicbcCacheTTL

              result mustBe Duration(duration, timeunit)
            }
          }
        }
      }
    }
  }
}
