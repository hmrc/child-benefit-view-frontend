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

package models.viewmodels

import base.BaseSpec
import models.viewmodels.InputWidth._

class InputWidthSpec extends BaseSpec {
  "InputWidth" - {
    "Fixed set" - {
      "Each Fixed directive's toString method returns the correct govuk css bootstrap class for the fixed width" - {
        def expectedCssForFixedWidth(width: Int) = s"govuk-input--width-$width"
        val fixedWidthTestCases = Table(
          ("testName", "fixedWidth", "expectedCss"),
          ("Fixed2", Fixed2, expectedCssForFixedWidth(2)),
          ("Fixed3", Fixed3, expectedCssForFixedWidth(3)),
          ("Fixed4", Fixed4, expectedCssForFixedWidth(4)),
          ("Fixed5", Fixed5, expectedCssForFixedWidth(5)),
          ("Fixed10", Fixed10, expectedCssForFixedWidth(10)),
          ("Fixed20", Fixed20, expectedCssForFixedWidth(20)),
          ("Fixed30", Fixed30, expectedCssForFixedWidth(30))
        )
        forAll(fixedWidthTestCases) { (testName, fixedWidth, expectedCss) =>
          s"$testName should result in css: $expectedCss" in {
            fixedWidth.toString mustBe expectedCss
          }
        }
      }
    }
    "Relative set" - {
      "Each Relative directive's to String method returns the correct govuk css bootstrap class for the relative width" - {
        def expectedCssForRelativeWidth(name: String) = s"govuk-!-width-$name"
        val relativeWidthTestCases = Table(
          ("testName", "relativeWidth", "expectedCss"),
          ("Full", Full, expectedCssForRelativeWidth("full")),
          ("ThreeQuarters", ThreeQuarters, expectedCssForRelativeWidth("three-quarters")),
          ("TwoThirds", TwoThirds, expectedCssForRelativeWidth("two-thirds")),
          ("OneHalf", OneHalf, expectedCssForRelativeWidth("one-half")),
          ("OneThird", OneThird, expectedCssForRelativeWidth("one-third")),
          ("OneQuarter", OneQuarter, expectedCssForRelativeWidth("one-quarter"))
        )
        forAll(relativeWidthTestCases) { (testName, relativeWidth, expectedCss) =>
          s"$testName should result in css: $expectedCss" in {
            relativeWidth.toString mustBe expectedCss
          }
        }
      }
    }
  }
}
