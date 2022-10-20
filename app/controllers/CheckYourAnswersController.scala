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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import models.viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class CheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    getData:                  DataRetrievalAction,
    requireData:              DataRequiredAction,
    view:                     CheckYourAnswersView,
    authConnector:            AuthConnector
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig
) extends ChildBenefitBaseController(authConnector)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = {
    implicit val loginContinueUrl: Call = routes.CheckYourAnswersController.onPageLoad

    (authorisedAsChildBenefitUser andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq.empty
      )
      Ok(view(list))
    }
  }
}
