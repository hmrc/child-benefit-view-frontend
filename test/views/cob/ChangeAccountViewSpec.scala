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
import models.changeofbank._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.cob.ChangeAccountView

class ChangeAccountViewSpec extends ViewSpecBase {

  val page: ChangeAccountView = inject[ChangeAccountView]
  val name: String            = "Cindy Boo"
  val details: ClaimantBankAccountInformation = ClaimantBankAccountInformation(
    Some(AccountHolderName("Cindy")),
    Some(SortCode("123456")),
    Some(BankAccountNumber("123456789")),
    Some(BuildingSocietyRollNumber("666666"))
  )

  private def createView(name: String, details: ClaimantBankAccountInformation): Html =
    page(name, details)(request, messages)

  val view: Document = Jsoup.parse(createView(name, details.copy(buildingSocietyRollNumber = None)).toString)
  val altView = Jsoup.parse(createView(name, details).toString)
  val noNameView =
    Jsoup.parse(createView(name, details.copy(accountHolderName = None, buildingSocietyRollNumber = None)).toString)

  "Change Account View" should {

    "have a title" in {
      view.getElementsByTag("title").text must include(" - Child Benefit - GOV.UK")
      view.getElementsByTag("title").text must include(messages("changeAccount.title"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text must include(messages("changeAccount.heading"))
    }

    "have a caption/section header" in {
      view.getElementById("section-header").text() mustBe "Cindy Boo"
    }

    "have a warning" in {
      view.getElementById("warning-text").text() must include(messages("changeAccount.warning"))
    }

    "have a secondary heading" when {
      "claimant has bank account details" in {
        view.getElementById("change-account-h2").text mustBe messages("changeAccount.subHeading")
      }
      "claimant has building society roll number" in {
        altView.getElementById("change-account-h2").text mustBe messages("changeAccount.subHeading")
      }

    }
    "have obscured details" when {
      "claimant has bank account" in {
        view.getElementById("account-details-table").text must include("**-**-56")
        view.getElementById("account-details-table").text must include("****6789")
      }
    }

    "have a details table" when {
      "claimant has bank account" in {
        view.getElementById("account-details-table").text must include(messages("changeAccount.table.name"))
        view.getElementById("account-details-table").text must include(messages("changeAccount.table.sort.code"))
        view.getElementById("account-details-table").text must include(messages("changeAccount.table.account.number"))
      }
      "claimant has none standard account" in {
        altView.getElementById("account-details-table").text must include(messages("changeAccount.table.name"))
        altView.getElementById("account-details-table").text must not include messages("changeAccount.table.sort.code")
        altView.getElementById("account-details-table").text must not include messages(
          "changeAccount.table.account.number"
        )
        altView.getElementById("info-notice").text mustBe messages("changeAccount.notification.text")
      }
      "claimant has no account name" in {
        noNameView.getElementById("account-details-table").text must include(name)
      }
    }
    "have a notice" when {
      "claimant had none standard account" in {
        altView.getElementById("info-notice").text must include(messages("changeAccount.notification.text"))
      }
    }
    "have a continue button" in {
      view.getElementById("continue-button").text() mustBe messages("changeAccount.button.1")
    }
    "have a do-not-change button" in {
      view.getElementById("do-not-change-button").text() mustBe messages("changeAccount.button.2")
    }

  }

}
