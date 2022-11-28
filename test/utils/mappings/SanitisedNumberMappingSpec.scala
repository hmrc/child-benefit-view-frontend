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

package utils.mappings

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}
import utils.mappings.SanitisedNumberMapping.sanitisedNumber

class SanitisedNumberMappingSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {
  val requiredError = "error.required"
  val formatError   = "error.format"

  val valueKey = "value"

  "sanitisedNumber" - {
    val sutForm: Form[String] = Form(valueKey -> sanitisedNumber(requiredError, formatError))

    "must bind a valid numerical string" in {
      val validNumerical = "123456"
      val sutResult      = sutForm.bind(Map(valueKey -> validNumerical))
      sutResult.get mustEqual validNumerical
    }

    def mustSanitiseAndBind(caseName: String, testValue: String, expectedResult: String) = {
      s"""$caseName: \"$testValue\" => \"$expectedResult\"""" in {
        val sutResult = sutForm.bind(Map(valueKey -> testValue))
        sutResult.get mustEqual expectedResult
      }
    }

    "must sanitise and bind cleanable values as expected" - {
      mustSanitiseAndBind("whitespace - leading", "   123456", "123456")
      mustSanitiseAndBind("whitespace - interspace", "1 2 3 4 5 6", "123456")
      mustSanitiseAndBind("whitespace - trailing", "123456   ", "123456")
      mustSanitiseAndBind("full stops", "12.34.56", "123456")
      mustSanitiseAndBind("underscores", "12_34_56", "123456")
      mustSanitiseAndBind("dashes", "12-34-56", "123456")
      mustSanitiseAndBind("forward stroke", "12/34/56", "123456")
    }

    def mustNotBindEmptyValue(caseName: String, testValue: String) = {
      s"empty variant: $caseName" in {
        val sutResult = sutForm.bind(Map(valueKey -> testValue))
        sutResult.errors must contain(FormError(valueKey, requiredError))
      }
    }

    "must not bind an empty value - returning a required error" - {
      mustNotBindEmptyValue("an empty string", "")
      mustNotBindEmptyValue("a whitespace (singular)", " ")
      mustNotBindEmptyValue("a whitespace (multiple)", "      ")
      mustNotBindEmptyValue("a whitespace (tabular)", "\t")
      mustNotBindEmptyValue("a whitespace (carriage return)", "\r")
      mustNotBindEmptyValue("a whitespace (new line)", "\n")
    }

    def mustNotBindInvalidValue(caseName: String, testValue: String) = {
      s"$caseName: $testValue" in {
        val sutResult = sutForm.bind(Map(valueKey -> testValue))
        println(sutResult)
        sutResult.errors must contain(FormError(valueKey, formatError, testValue))
      }
    }

    "must not bind an invalid value - returning a format error" - {
      mustNotBindInvalidValue("letters - leading", "a123456")
      mustNotBindInvalidValue("letters - interspaced", "123a456")
      mustNotBindInvalidValue("letters - trailing", "123456a")
      mustNotBindInvalidValue("sample characters - $", "123456$")
      mustNotBindInvalidValue("""sample characters - """", """123456"""")
    }
  }
}
