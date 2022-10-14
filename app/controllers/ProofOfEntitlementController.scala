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
import controllers.ProofOfEntitlementController.fireAuditEvent
import controllers.auth.AuthContext
import handlers.ErrorHandler
import models.audit.ClaimantEntitlementDetails
import models.entitlement.{Child, ChildBenefitEntitlement}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import play.api.{Configuration, Environment}
import services.AuditorService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.DeviceFingerprint
import views.html.ProofOfEntitlement

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ProofOfEntitlementController @Inject() (
    authConnector:                    AuthConnector,
    childBenefitEntitlementConnector: ChildBenefitEntitlementConnector,
    errorHandler:                     ErrorHandler,
    proofOfEntitlement:               ProofOfEntitlement
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig,
    auditor:           AuditorService
) extends ChildBenefitBaseController(authConnector) {
  val view: Action[AnyContent] =
    Action.async { implicit request =>
      authorisedAsChildBenefitUser { authContext =>
        childBenefitEntitlementConnector.getChildBenefitEntitlement.fold(
          err => errorHandler.handleError(err),
          entitlement => {
            fireAuditEvent(request, entitlement, authContext)

            Ok(proofOfEntitlement(entitlement))
          }
        )
      }(routes.ProofOfEntitlementController.view)
    }
}

object ProofOfEntitlementController {
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
    val formattedDate = date.format(
      DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.locale)
    )

    if (checkForSpecialAwardStartDate && specialAwardStartDates.contains(date)) {
      messages("proofOfEntitlement.onorbefore", formattedDate)
    } else {
      formattedDate
    }
  }

  def fireAuditEvent(
      request:        MessagesRequest[AnyContent],
      entitlement:    ChildBenefitEntitlement,
      authContext:    AuthContext[Any]
  )(implicit auditor: AuditorService, hc: HeaderCarrier, ec: ExecutionContext): Unit = {

    val nino              = authContext.nino.nino
    val deviceFingerprint = DeviceFingerprint.deviceFingerprintFrom(request)
    val ref = request.headers
      .get("referer") //MDTP header
      .getOrElse(
        request.headers
          .get("Referer") //common browser header
          .getOrElse("Referrer not found")
      )
    val entDetails: Option[ClaimantEntitlementDetails] =
      Some(
        ClaimantEntitlementDetails(
          name = entitlement.claimant.name.value,
          address = entitlement.claimant.fullAddress.toSingleLineString,
          amount = entitlement.claimant.awardValue,
          start = entitlement.claimant.awardStartDate.toString,
          end = entitlement.claimant.awardEndDate.toString,
          children = entitlement.children
        )
      )
    auditor.viewProofOfEntitlement(nino, status = "Success", ref, deviceFingerprint, entDetails)

  }
}
