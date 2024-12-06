/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import config.{FeatureAllowlistFilter, FrontendAppConfig}
import play.api.mvc.Results.NotFound
import play.api.{Configuration, Logging}
import play.api.mvc._
import views.html.ftnae.FtnaeDisabledView

import scala.concurrent.{ExecutionContext, Future}

trait FtnaeIdentifierAction extends ActionFunction[MessagesRequest, MessagesRequest]

class FtnaeIdentifierActionImpl @Inject() (
    configuration:               Configuration,
    val allowList:               FeatureAllowlistFilter,
    ftnaeDisabledView:           FtnaeDisabledView
)(implicit val executionContext: ExecutionContext, val config: FrontendAppConfig)
    extends FtnaeIdentifierAction
    with Logging {

  override def invokeBlock[A](
      request: MessagesRequest[A],
      block:   MessagesRequest[A] => Future[Result]
  ): Future[Result] = {

    val isFeatureEnabled =
      configuration.getOptional[Boolean](s"feature-flags.ftnae.enabled").getOrElse(false)

    val hideMenuBar = request.session.get("authToken").isEmpty

    enableFtnaeFeature(request, block, isFeatureEnabled, hideWrapperMenuBar = hideMenuBar)
  }

  private def enableFtnaeFeature[A](
      request:            MessagesRequest[A],
      block:              MessagesRequest[A] => Future[Result],
      isFeatureEnabled:   Boolean,
      hideWrapperMenuBar: Boolean
  ) = {
    if (isFeatureEnabled) {
      allowList(_ => block(request))("ftnae", request)
    } else {
      allowList(_ =>
        Future.successful(
          NotFound(
            ftnaeDisabledView(hideWrapperMenuBar)(request, request.messages)
          )
        )
      )("ftnae", request)
    }
  }
}
