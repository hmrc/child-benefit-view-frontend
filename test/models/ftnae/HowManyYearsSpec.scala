package models.ftnae

import base.BaseSpec
import org.mockito.Mockito.when
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class HowManyYearsSpec extends BaseSpec {
  "HowManyYears" - {
    "values" - {
      "Returns all expected cases of HowManyYears" in {
        val expectedHowManyYears = List(HowManyYears.Oneyear, HowManyYears.Twoyears, HowManyYears.Other)

        val result = HowManyYears.values

        result must contain theSameElementsAs (expectedHowManyYears)
      }
    }
    "options" - {
      "Returns a RadioItem for each HowManyYears value is the expected sequence" in {
        implicit val mockMessages = mock[Messages]
        def messageForValue(value: HowManyYears) = s"expected message for ${value.toString}"
        def setupMessages(value: HowManyYears) = {
          when(mockMessages(s"howManyYears.${value.toString}")).thenReturn(messageForValue(value))
        }
        HowManyYears.values.foreach(setupMessages(_))
        val expectedOrderedSeq = Seq(HowManyYears.Oneyear, HowManyYears.Twoyears, HowManyYears.Other)

        val results = HowManyYears.options

        results.zipWithIndex.map(result => {
          val (item, index) = (result._1, result._2)
          item.content mustBe Text(messageForValue(expectedOrderedSeq(index)))
          item.value mustBe Some(expectedOrderedSeq(index).toString)
          item.id mustBe Some(s"value_$index")
        })
      }
    }
  }
}
