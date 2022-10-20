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

package controllers.actions

import play.api.Configuration
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionFilter, MessagesRequest, Result}
import views.html.ErrorTemplate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureFlagAction @Inject() (configuration: Configuration, errorTemplate: ErrorTemplate)(implicit
    ec:                                           ExecutionContext
) {
  private val featureFlags: Map[String, Boolean] = configuration.get[Map[String, Boolean]]("feature-flags")

  private def whenEnabled(featureFlag: String): ActionFilter[MessagesRequest] =
    new ActionFilter[MessagesRequest] {
      override protected def filter[A](request: MessagesRequest[A]): Future[Option[Result]] =
        if (featureFlags.getOrElse(featureFlag, false)) {
          Future.successful(None)
        } else {
          Future.successful(
            Some(
              NotFound(
                errorTemplate("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
                  request,
                  request.messages
                )
              )
            )
          )
        }

      override protected def executionContext: ExecutionContext = ec
    }

  val dummyFlagEnabled: ActionFilter[MessagesRequest] = whenEnabled("dummy-flag")
}
