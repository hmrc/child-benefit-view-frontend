package models.viewmodels

import base.BaseSpec
import models.viewmodels.LabelSize._

class LabelSizeSpec extends BaseSpec {
  "LabelSize" - {
    "Each Label Size's directive's toString method returns the correct govuk css bootstrap class for the label size" - {
      def expectedCssForLabelSize(label: String) = s"govuk-label--$label"
      val lableSizesTestCases = Table(
        ("testName",   "labelSize", "expectedCss"),
        ("ExtraLarge", ExtraLarge,  expectedCssForLabelSize("xl")),
        ("Large",      Large,       expectedCssForLabelSize("l")),
        ("Medium",     Medium,      expectedCssForLabelSize("m")),
        ("Small",      Small,       expectedCssForLabelSize("s"))
      )
      forAll(lableSizesTestCases) { (testName, labelSize, expectedCss) =>
        s"$testName should result in css: $expectedCss" in {
          labelSize.toString mustBe expectedCss
        }
      }
    }
  }
}
