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
import models.requests.IdentifierRequest
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

class FakeVerifyBarNotLockedAction(verify: Boolean) extends VerifyBarNotLockedAction {
  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    if (verify)
      Future.successful(None)
    else
      Future.successful(Some(Redirect(controllers.cob.routes.BARSLockOutController.onPageLoad())))
  }
  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}

object FakeVerifyBarNotLockedAction {
  def apply(verify: Boolean) = new FakeVerifyBarNotLockedAction(verify)
}
