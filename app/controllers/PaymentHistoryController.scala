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

import config.FrontendAppConfig
import handlers.ErrorHandler
import play.api.mvc.{Action, AnyContent}
import services.PaymentHistoryService
import play.api.mvc.MessagesControllerComponents
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentHistoryController @Inject() (
    authConnector:         AuthConnector,
    paymentHistoryService: PaymentHistoryService,
    errorHandler:          ErrorHandler
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsChildBenefitUser { _ =>
      paymentHistoryService.retrieveAndValidatePaymentHistory
        .fold(
          err => errorHandler.handleError(err),
          result => Ok(result)
        )
    }(routes.PaymentHistoryController.view)
  }
}
