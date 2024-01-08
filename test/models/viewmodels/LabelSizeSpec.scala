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
import models.viewmodels.LabelSize._

class LabelSizeSpec extends BaseSpec {
  "LabelSize" - {
    "Each Label Size's directive's toString method returns the correct govuk css bootstrap class for the label size" - {
      def expectedCssForLabelSize(label: String) = s"govuk-label--$label"
      val lableSizesTestCases = Table(
        ("testName", "labelSize", "expectedCss"),
        ("ExtraLarge", ExtraLarge, expectedCssForLabelSize("xl")),
        ("Large", Large, expectedCssForLabelSize("l")),
        ("Medium", Medium, expectedCssForLabelSize("m")),
        ("Small", Small, expectedCssForLabelSize("s"))
      )
      forAll(lableSizesTestCases) { (testName, labelSize, expectedCss) =>
        s"$testName should result in css: $expectedCss" in {
          labelSize.toString mustBe expectedCss
        }
      }
    }
  }
}
