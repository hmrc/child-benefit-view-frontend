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

package views.cob

import base.ViewSpecBase
import views.html.cob.NewAccountDetailsView
import org.jsoup.Jsoup
import forms.cob.NewAccountDetailsFormProvider
import models.NormalMode
import models.cob.WhatTypeOfAccount

class NewAccountDetailsViewSpec extends ViewSpecBase {
    "New Account Details View" should {
        "have autocomplete enabled on the name input" in {
            val form = inject[NewAccountDetailsFormProvider].apply()
            val view = inject[NewAccountDetailsView].apply(form, NormalMode, WhatTypeOfAccount.Sole)(request, messages)
            val dom = Jsoup.parse(view.toString())
            val input = dom.getElementsByAttributeValue("name", "newAccountHoldersName")
            input.attr("autocomplete") mustBe "name"
        }
        
        "disable spellchecking on the name input" in {
            val form = inject[NewAccountDetailsFormProvider].apply()
            val view = inject[NewAccountDetailsView].apply(form, NormalMode, WhatTypeOfAccount.Sole)(request, messages)
            val dom = Jsoup.parse(view.toString())
            val input = dom.getElementsByAttributeValue("name", "newAccountHoldersName")
            input.attr("spellcheck") mustBe "false"
        }
    }
}
