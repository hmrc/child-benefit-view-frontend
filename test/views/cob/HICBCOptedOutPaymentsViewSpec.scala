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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.cob.HICBCOptedOutPaymentsView

class HICBCOptedOutPaymentsViewSpec extends ViewSpecBase {

  protected val page: HICBCOptedOutPaymentsView = inject[HICBCOptedOutPaymentsView]
  val view:           Document                  = Jsoup.parse(createView.toString)

  private def createView: Html = page()(request, messages)

  "Opted Out Payments View" must {

    "have a title" in {
      view.getElementsByTag("title").text must include(" - Child Benefit - GOV.UK")
      view.getElementsByTag("title").text must include(messages("hICBCOptedOutPayments.title"))
    }

    "have a heading" in {
      view.getElementsByTag("h1").text mustBe messages("hICBCOptedOutPayments.heading")
    }

    "have some text" in {
      view.text() must include(messages("hICBCOptedOutPayments.paragraph.1"))
    }
    "have some text with a link" in {
      view.text() must include(
        messages("hICBCOptedOutPayments.paragraph.2", messages("hICBCOptedOutPayments.guide.link.text"))
      )
      view.getElementById("guide-link").text mustBe messages("hICBCOptedOutPayments.guide.link.text")
    }

  }
}
