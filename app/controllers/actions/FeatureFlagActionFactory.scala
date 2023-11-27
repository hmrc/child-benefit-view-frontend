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
import play.twirl.api.{BaseScalaTemplate, Format, HtmlFormat}
import views.html.ErrorTemplate
import views.html.ftnae.FtnaeSwitchedOffView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FeatureFlagActionFactory @Inject() (
    configuration: Configuration,
    errorTemplate: ErrorTemplate,
    ftnaeSwitchedOffView: FtnaeSwitchedOffView,
    val allowList: FeatureAllowlistFilter
)(implicit
    ec: ExecutionContext
) {

  //noinspection ScalaStyle                                                                                             //TODO: Remove noinspection
  private def whenEnabled(featureFlag: String, specifiedView:
  Option[BaseScalaTemplate[HtmlFormat.Appendable, Format[HtmlFormat.Appendable]]] = None): FeatureFlagAction =          //TODO: Fix formatting here
    new FeatureFlagAction {
      override def invokeBlock[A](
          request: MessagesRequest[A],
          block:   MessagesRequest[A] => Future[Result]
      ): Future[Result] = {
        val isFeatureEnabled =
          configuration.getOptional[Boolean](s"feature-flags.$featureFlag.enabled").getOrElse(false)

        val featureDisabledView = specifiedView match {
          case None =>
            errorTemplate("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
                request,
                request.messages
              )
          case Some(_) =>
            ftnaeSwitchedOffView()(
              request,
              request.messages
            )
//          case Some(_) =>
//            _()(
//              request,#
//              request.messages
//            )
        }

        if (isFeatureEnabled) {
          allowList(_ => block(request))(featureFlag, request)
        } else {
          allowList(_ =>
            Future.successful(
              NotFound(
                featureDisabledView
              )
            )
          )(featureFlag, request)
        }
      }

      override protected def executionContext: ExecutionContext = ec
    }

  val changeOfBankEnabled: FeatureFlagAction = whenEnabled("change-of-bank")
  val ftnaeEnabled:        FeatureFlagAction = whenEnabled("ftnae", Some(ftnaeSwitchedOffView))
  val addChildEnabled:     FeatureFlagAction = whenEnabled("add-child")
  val hicbcEnabled:        FeatureFlagAction = whenEnabled("hicbc")
}

trait FeatureFlagAction extends ActionFunction[MessagesRequest, MessagesRequest]
