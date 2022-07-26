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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import models.cob.ConfirmNewAccountDetails.{No, Yes}
import utils.pages._
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case NewAccountDetailsPage        => _ => controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
    case ConfirmNewAccountDetailsPage => userAnswers => confirmAccountDetails(userAnswers)
    case _                            => _ => controllers.routes.IndexController.onPageLoad
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => controllers.routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }

  private def confirmAccountDetails(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfirmNewAccountDetailsPage) match {
      case Some(Yes) => controllers.cob.routes.AccountChangedController.onPageLoad()
      case Some(No)  => controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode)
      case _         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
}
