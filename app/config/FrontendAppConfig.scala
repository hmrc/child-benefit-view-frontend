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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  val host:             String = configuration.get[String]("host")
  val appName:          String = configuration.get[String]("appName")
  val loginUrl:         String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl:       String = configuration.get[String]("urls.signOut")
  val timeout:          Int    = configuration.get[Int]("timeout-dialog.timeout")
  val countdown:        Int    = configuration.get[Int]("timeout-dialog.countdown")
  val cacheTtl:         Int    = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val childBenefitEntitlementBaseUrl: String = servicesConfig.baseUrl("child-benefit-entitlement")
  private val exitSurveyBaseUrl:              String = servicesConfig.baseUrl("feedback-frontend")

  def childBenefitEntitlementUrl: String =
    s"$childBenefitEntitlementBaseUrl/child-benefit-service/view-entitlements-and-payments"

  def exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/child-benefit-view-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=child-benefit-view-frontend&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  def languageMap: Map[String, Lang] =
    Map("en" -> Lang("en"), "cy" -> Lang("cy"))

}
