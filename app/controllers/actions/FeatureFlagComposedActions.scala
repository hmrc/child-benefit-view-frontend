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
import play.api.mvc.{ActionBuilder, AnyContent, MessagesControllerComponents, MessagesRequest}

import javax.inject.Inject

/*
A series of composed actions that will filter messages based on feature flags then chain to the initial
Identifier action, allowing for further transformation and composition as required
 */
class FeatureFlagComposedActions @Inject() (
    controllerComponents:  MessagesControllerComponents,
    featureFlags:          FeatureFlagActionFactory,
    auth:                  StandardAuthJourney,
    ftnaeIdentifierAction: FtnaeIdentifierAction,
    redirectToPegaAction:  RedirectToPegaAction
) {
  private def actionStart: ActionBuilder[MessagesRequest, AnyContent] =
    controllerComponents.messagesActionBuilder.compose(controllerComponents.actionBuilder)

  private def featureAction(featureFlagAction: FeatureFlagAction): ActionBuilder[IdentifierRequest, AnyContent] =
    actionStart andThen redirectToPegaAction andThen featureFlagAction andThen auth.pertaxAuthActionWithUserDetails

  def changeBankAction: ActionBuilder[IdentifierRequest, AnyContent] = featureAction(featureFlags.changeOfBankEnabled)

  def ftnaeAction: ActionBuilder[MessagesRequest, AnyContent] = actionStart andThen ftnaeIdentifierAction
}
