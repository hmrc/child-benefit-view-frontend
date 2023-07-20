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

package controllers.cob

import controllers.actions._
import pages.cob.NewAccountDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AuditService, ChangeOfBankService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.handlers.ErrorHandler
import views.html.cob.AccountChangedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AccountChangedController @Inject() (
    override val messagesApi: MessagesApi,
    featureActions:           FeatureFlagComposedActions,
    getData:                  CBDataRetrievalAction,
    requireData:              DataRequiredAction,
    verifyBarNotLockedAction: VerifyBarNotLockedAction,
    verifyHICBCActionImpl:    VerifyHICBCAction,
    val controllerComponents: MessagesControllerComponents,
    changeOfBankService:      ChangeOfBankService,
    errorHandler:             ErrorHandler,
    view:                     AccountChangedView
)(implicit ec:                ExecutionContext, auditService: AuditService)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (featureActions.changeBankAction andThen
      verifyBarNotLockedAction andThen
      verifyHICBCActionImpl andThen
      getData andThen
      requireData)
      .async { implicit request =>
        {
          val newAccountDetails = request.userAnswers.get(NewAccountDetailsPage)
          for {
            submissionResponse <- changeOfBankService.submitClaimantChangeOfBank(newAccountDetails, request).value
            cacheResponse      <- changeOfBankService.dropChangeOfBankCache().value
          } yield {
            submissionResponse match {
              case Left(error) => errorHandler.handleError(error)
              case Right(_) =>
                cacheResponse match {
                  case Left(error) => errorHandler.handleError(error)
                  case Right(_)    => Ok(view())
                }
            }
          }
        }
      }
}
