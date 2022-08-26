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
import connectors.ChildBenefitEntitlementConnector
import models.errors.ConnectorError
import play.api.{Configuration, Environment}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.{ErrorTemplate, ProofOfEntitlement}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProofOfEntitlementController @Inject() (
    authConnector:                    AuthConnector,
    childBenefitEntitlementConnector: ChildBenefitEntitlementConnector,
    errorTemplate:                    ErrorTemplate,
    proofOfEntitlement:               ProofOfEntitlement
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] =
    Action.async { implicit request =>
      authorisedAsChildBenefitUser { _ =>
        childBenefitEntitlementConnector.getChildBenefitEntitlement.foldF[Result](
          {
            case ConnectorError(statusCode, message) =>
              Future.successful(
                Status(statusCode)(
                  errorTemplate(
                    request.messages("global.error.InternalServerError500.title"),
                    request.messages("global.error.InternalServerError500.heading"),
                    message
                  )
                )
              )
          },
          entitlement => Future.successful(Ok(proofOfEntitlement(entitlement)))
        )
      }(routes.ProofOfEntitlementController.view.absoluteURL())
    }
}

object ProofOfEntitlementController {
  def formatDate(date: LocalDate)(implicit messages: Messages): String =
    date.format(
      DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.locale)
    )

  def formatMoney(amount: Double, currency: String = "£"): String =
    f"$currency$amount%.2f"
}
