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

package views.cob

import base.ViewSpecBase
import models.NormalMode
import models.viewmodels.govuk.summarylist._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key
import views.html.cob.ConfirmNewAccountDetailsView

class ConfirmNewAccountDetailsViewSpec extends ViewSpecBase {

  val page: ConfirmNewAccountDetailsView = inject[ConfirmNewAccountDetailsView]
  val name: String                       = "Cindy Boo"
  val details = Map("accountHolder" -> "C. Boo", "sortCode" -> "123456", "accountNum" -> "7654321")

  def summaryList() =
    SummaryListViewModel(
      Seq(
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountType.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape("TEST"))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountType.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountHoldersName.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(details("accountHolder")))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountHoldersName.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.sortCode.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(details("sortCode")))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.sortCode.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountNumber.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(details("accountNum")))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountNumber.change.hidden"))
          )
        )
      )
    )
      .withCssClass("govuk-!-margin-bottom-9")
      .withAttribute("id" -> "account-details-list")

  private def createView: Html =
    page(NormalMode, summaryList())(
      request,
      messages
    )

  val view: Document = Jsoup.parse(createView.toString)

  "Change Account View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(" - Child Benefit - GOV.UK")
      view.getElementsByTag("title").text must include(messages("confirmNewAccountDetails.title"))
    }

    "have a heading" in {
      view.getElementsByClass("govuk-heading-xl").text must include(messages("confirmNewAccountDetails.heading"))
    }

    "have a paragraph" in {
      view.getElementsByClass("govuk-body").text must include(messages("confirmNewAccountDetails.p1"))
    }

    "have a list" that {
      val summaryListElement = view.getElementById("account-details-list")
      "has account type row" in {
        val expectedRowElement = summaryListElement.child(0)
        expectedRowElement.child(0).text must include(messages("confirmNewAccountDetails.summary.accountType.label"))
        expectedRowElement.child(1).text must include("TEST")
        expectedRowElement.child(2).text must include(
          messages("confirmNewAccountDetails.summary.accountType.change.hidden")
        )

      }
      "has account holder row" in {
        val expectedRowElement = summaryListElement.child(1)
        expectedRowElement.child(0).text must include(
          messages("confirmNewAccountDetails.summary.accountHoldersName.label")
        )
        expectedRowElement.child(1).text must include(details("accountHolder"))
        expectedRowElement.child(2).text must include(
          messages("confirmNewAccountDetails.summary.accountHoldersName.change.hidden")
        )
      }
      "has sort code row" in {
        val expectedRowElement = summaryListElement.child(2)
        expectedRowElement.child(0).text must include(messages("confirmNewAccountDetails.summary.sortCode.label"))
        expectedRowElement.child(1).text must include(details("sortCode"))
        expectedRowElement.child(2).text must include(
          messages("confirmNewAccountDetails.summary.sortCode.change.hidden")
        )
      }
      "has account number row" in {
        val expectedRowElement = summaryListElement.child(3)
        expectedRowElement.child(0).text must include(messages("confirmNewAccountDetails.summary.accountNumber.label"))
        expectedRowElement.child(1).text must include(details("accountNum"))
        expectedRowElement.child(2).text must include(
          messages("confirmNewAccountDetails.summary.accountNumber.change.hidden")
        )
      }
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }

  }

}
