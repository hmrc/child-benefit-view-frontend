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

package controllers.actions

import connectors.ChangeOfBankConnector
import models.requests.IdentifierRequest
import play.api.mvc.ActionFilter
import play.api.Logging
import play.api.mvc.{Result}
import services.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.handlers.ErrorHandler

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait VerifyBarNotLockedAction extends ActionFilter[IdentifierRequest]

class VerifyBarNotLockedActionImpl @Inject() (
    cobConnector:                ChangeOfBankConnector,
    errorHandler:                ErrorHandler,
    auditService:                AuditService
)(implicit val executionContext: ExecutionContext)
    extends VerifyBarNotLockedAction
    with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    {
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      cobConnector
        .verifyBARNotLocked()
        .foldF(
          e => Future.successful(Some(errorHandler.handleError(e, None)(auditService, request, hc, executionContext))),
          _ => Future.successful(None)
        )
    }
  }
}
