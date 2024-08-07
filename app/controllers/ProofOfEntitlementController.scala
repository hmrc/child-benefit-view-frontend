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

package controllers

import connectors.ChildBenefitEntitlementConnector
import controllers.actions.IdentifierAction
import utils.handlers.ErrorHandler
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import services.AuditService
import uk.gov.hmrc.auth.core.AuthConnector
import utils.helpers.ChildBenefitEntitlementHelper.formatChildBenefitEntitlement
import views.ViewUtils
import views.html.ProofOfEntitlement

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import config.FrontendAppConfig

@Singleton
class ProofOfEntitlementController @Inject() (
    authConnector:                    AuthConnector,
    childBenefitEntitlementConnector: ChildBenefitEntitlementConnector,
    errorHandler:                     ErrorHandler,
    proofOfEntitlement:               ProofOfEntitlement,
    identify:                         IdentifierAction,
    frontendAppConfig:                FrontendAppConfig
)(implicit
    config:  Configuration,
    env:     Environment,
    ec:      ExecutionContext,
    cc:      MessagesControllerComponents,
    auditor: AuditService
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] =
    Action andThen identify async { implicit request =>
      if (frontendAppConfig.redirectToPEGA) {
        Future.successful(Redirect(frontendAppConfig.pegaPoeUrl, 303))
      } else {
        childBenefitEntitlementConnector.getChildBenefitEntitlement.fold(
          err => errorHandler.handleError(err, Some("proofOfEntitlement")),
          entitlement => {
            auditor.auditProofOfEntitlement(
              request.nino.nino,
              "Successful",
              request,
              Some(entitlement)
            )
            Ok(proofOfEntitlement(formatChildBenefitEntitlement(entitlement)))
          }
        )
      }
    }
}

object ProofOfEntitlementController {

  /**
    * These are effectively default dates and carried over as a result of the CBS migration from COBOL.
    * These dates were used if the dates were missing.
    */
  private val specialAwardStartDates: Set[LocalDate] =
    Set(
      LocalDate.of(2021, 2, 22),
      LocalDate.of(2021, 3, 1),
      LocalDate.of(2021, 3, 8),
      LocalDate.of(2021, 3, 15)
    )

  def formatEntitlementDate(date: LocalDate, checkForSpecialAwardStartDate: Boolean = false)(implicit
      messages:                   Messages
  ): String = {
    val formattedDate = ViewUtils.formatDate(date)

    if (checkForSpecialAwardStartDate && specialAwardStartDates.contains(date)) {
      messages("proofOfEntitlement.onorbefore", formattedDate)
    } else {
      formattedDate
    }
  }

}
