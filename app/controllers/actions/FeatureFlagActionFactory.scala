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

import config.FeatureAllowlistFilter
import play.api.Configuration
import play.api.mvc.Results.NotFound
import play.api.mvc.{ActionFunction, MessagesRequest, Result}
import views.html.ErrorTemplate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureFlagActionFactory @Inject() (
    configuration: Configuration,
    errorTemplate: ErrorTemplate,
    val allowList: FeatureAllowlistFilter
)(implicit
    ec: ExecutionContext
) {
  private val featureFlags: Map[String, Boolean] = configuration.get[Map[String, Boolean]]("feature-flags")

  private def whenEnabled(featureFlag: String): FeatureFlagAction =
    new FeatureFlagAction {
      override def invokeBlock[A](
          request: MessagesRequest[A],
          block:   MessagesRequest[A] => Future[Result]
      ): Future[Result] = {
        val test = featureFlags.getOrElse(featureFlag, false)
        if (test) {
          allowList(_ => block(request))(featureFlag, request)
        } else {
          allowList(_ =>
            Future.successful(
              NotFound(
                errorTemplate("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
                  request,
                  request.messages
                )
              )
            )
          )(featureFlag, request)
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  val dummyFlagEnabled:    FeatureFlagAction = whenEnabled("dummy-flag")
  val changeOfBankEnabled: FeatureFlagAction = whenEnabled("change-of-bank")
  val ftnaeEnabled:        FeatureFlagAction = whenEnabled("ftnae")
  val addChildEnabled:     FeatureFlagAction = whenEnabled("add-child")
  val hicbcEnabled:        FeatureFlagAction = whenEnabled("hicbc")
}

trait FeatureFlagAction extends ActionFunction[MessagesRequest, MessagesRequest]
