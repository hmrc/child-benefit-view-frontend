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

package views.cob

import base.ViewSpecBase
import models.cob.AccountDetails
import play.twirl.api.Html
import views.html.cob.ChangeAccountView

class ChangeAccountViewSpec extends ViewSpecBase {

  val page: ChangeAccountView = inject[ChangeAccountView]
  val name: String = "name"
  val details: AccountDetails = AccountDetails("Lizbeth Jones", "12-34-56", "123456789")

  private def createView(name: String, details: Option[AccountDetails]): Html =
    page(name, details)(request, messages)

  "Change Account View" should {

    val view = createView(name, Some(details))
    val altView = createView(name, None)

    "have a title" in {
      view.select("title").text must include(" - Child Benefit - GOV.UK")
      view.select("title").text must include(messages("changeAccount.title"))
    }

    "have a heading" in {
      view.select("h1").text mustBe messages("changeAccount.heading")
    }

    "have a caption/section header" in {
      view.getElementById("section-header").text mustBe name
    }

    "have a secondary heading" when {
      "account details are present" in {
        view.getElementById("change-account-h2").text mustBe messages("changeAccount.subHeading")
      }
      "account details aren't given" in {
        altView.getElementById("change-account-h2").text mustBe messages("changeAccount.subHeading.alt")
      }

    }
    "have a details table" when {
      "account details are present" ignore {
        ???
      }
      "account details aren't given" ignore {
        altView.getElementById("info-notice").text mustBe messages("changeAccount.notification.text")

      }
    }

  }

}
