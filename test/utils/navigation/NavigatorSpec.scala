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

package utils.navigation

import base.SpecBase
import controllers.routes
import models.cob.ConfirmNewAccountDetails.Yes
import utils.pages._
import models._
import models.cob.ConfirmNewAccountDetails._
import models.cob.AccountDetails
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad
      }

      "must go from NewAccountDetails to ConfirmNewAccountDetails page" in {
        navigator.nextPage(
          NewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(AccountDetails("Name", "123456", "00000000001")))
        ) mustBe controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
      }

      "must go from ConfirmNewAccountDetails to AccountChanged page" in {
        navigator.nextPage(
          ConfirmNewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(AccountDetails("Name", "123456", "00000000001")))
            .set(ConfirmNewAccountDetailsPage, Yes)
            .get
        ) mustBe controllers.cob.routes.AccountChangedController.onPageLoad()
      }

      "must go from ConfirmNewAccountDetails back to NewAccountDetails page when No is selected" in {
        navigator.nextPage(
          ConfirmNewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(AccountDetails("Name", "123456", "00000000001")))
            .set(ConfirmNewAccountDetailsPage, No)
            .get
        ) mustBe controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode)
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
