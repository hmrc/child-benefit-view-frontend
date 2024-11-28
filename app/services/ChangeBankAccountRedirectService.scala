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

package services

import config.FrontendAppConfig
import models.admin.ChangeBankAccountRedirectToPegaToggle
import play.api.cache._
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeBankAccountRedirectService @Inject() (
    featureFlagService: FeatureFlagService,
    cache:              AsyncCacheApi,
    appConfig:          FrontendAppConfig
)(implicit ec:          ExecutionContext) {

  def getChangeBankAccountRedirectToggle: Future[Boolean] = {
    cache.getOrElseUpdate[Boolean]("change-bank-account-redirect.toggle", appConfig.changeBankAccountRedirectCacheTTL) {
      featureFlagService.get(ChangeBankAccountRedirectToPegaToggle).map(_.isEnabled)
    }
  }

}
