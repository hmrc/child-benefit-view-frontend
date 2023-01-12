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
import forms.cob.ConfirmNewAccountDetailsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.cob.ConfirmNewAccountDetailsView

class ConfirmNewAccountDetailsViewSpec extends ViewSpecBase {

  val page: ConfirmNewAccountDetailsView = inject[ConfirmNewAccountDetailsView]
  val name: String                       = "Cindy Boo"
  val deets = Map("accountHolder" -> "C. Boo", "sortCode" -> "123456", "accountNum" -> "7654321")

  val form = new ConfirmNewAccountDetailsFormProvider()()

  private def createView: Html =
    page(form, NormalMode, name, deets("accountHolder"), deets("sortCode"), deets("accountNum"))(request, messages)

  val view: Document = Jsoup.parse(createView.toString)

  "Change Account View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(" - Child Benefit - GOV.UK")
      view.getElementsByTag("title").text must include(messages("confirmNewAccountDetails.title"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe messages("confirmNewAccountDetails.heading")
    }

    "have a list" that {
      "has account holder row" in {
        view.getElementById("account-details-list").text() must include(messages("confirmNewAccountDetails.table.name"))
      }
      "has sort code row" in {
        view.getElementById("account-details-list").text() must include(
          messages("confirmNewAccountDetails.table.sortCode")
        )
      }
      "has account number row" in {
        view.getElementById("account-details-list").text() must include(
          messages("confirmNewAccountDetails.table.accountNumber")
        )
      }
    }
    "have a caption/section header" in {
      view.getElementById("section-header").text() mustBe "Cindy Boo"
    }

    "have a sub heading" in {
      view.getElementById("confirm-details-h2").text() mustBe messages("confirmNewAccountDetails.subHeading")
    }

    "have a continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }

  }

}
