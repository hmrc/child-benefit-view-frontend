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
import handlers.ErrorHandler
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import services.{AuditService, PaymentHistoryService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.DeviceFingerprint

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PaymentHistoryController @Inject() (
    authConnector:                    AuthConnector,
    paymentHistoryService:            PaymentHistoryService,
    childBenefitEntitlementConnector: ChildBenefitEntitlementConnector,
    errorHandler:                     ErrorHandler
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig,
    auditor:           AuditService
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] =
    Action.async { implicit request =>
      authorisedAsChildBenefitUser { authContext =>
        paymentHistoryService.retrieveAndValidatePaymentHistory.fold(
          err => errorHandler.handleError(err),
          result => {
            childBenefitEntitlementConnector.getChildBenefitEntitlement.fold(
              err => errorHandler.handleError(err),
              entitlement => {
                auditor.auditPaymentDetails(
                  authContext.nino.nino,
                  DeviceFingerprint.deviceFingerprintFrom(request),
                  request.headers
                    .get("referer") //MDTP header
                    .getOrElse(
                      request.headers
                        .get("Referer") //common browser header
                        .getOrElse("Referrer not found")
                    ),
                  entitlement
                )

              }
            )
            Ok(result)
          }
        )
      }(routes.PaymentHistoryController.view)
    }

}
