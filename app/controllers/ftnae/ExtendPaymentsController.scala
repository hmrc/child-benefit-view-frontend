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

package controllers.ftnae

import cats.data.EitherT
import controllers.actions._
import models.{CBEnvelope, UserAnswers}
import models.errors.CBError
import models.ftnae.FtneaClaimantInfo
import pages.ftnae.FtneaResponseUserAnswer
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{AuditService, FtnaeService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import views.html.ftnae.ExtendPaymentsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExtendPaymentsController @Inject() (
    override val messagesApi: MessagesApi,
    identify:                 IdentifierAction,
    getData:                  CBDataRetrievalAction,
    val controllerComponents: MessagesControllerComponents,
    featureActions:           FeatureFlagComposedActions,
    sessionRepository:        SessionRepository,
    view:                     ExtendPaymentsView,
    ftneaService:             FtnaeService,
    errorHandler:             ErrorHandler
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (featureActions.ftnaeAction andThen identify andThen getData).async { implicit request =>
      val result: EitherT[Future, CBError, FtneaClaimantInfo] = for {
        ftneaResponse <- ftneaService.getFtnaeInformation()
        updatedAnswers <- CBEnvelope.fromF(
          Future.fromTry(
            request.userAnswers
              .getOrElse(UserAnswers(request.userId))
              .set(FtneaResponseUserAnswer, ftneaResponse)
          )
        )
        _ <- CBEnvelope(sessionRepository.set(updatedAnswers))
      } yield ftneaResponse.claimant

      result.fold[Result](l => errorHandler.handleError(l), claimant => Ok(view(claimant)))

    }

}
