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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{RedirectUrl, UnsafePermitAll}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {
  private def getExternalUrl(service: String): String = servicesConfig.getString(s"external-urls.$service.url")

  def host:        String = configuration.get[String]("host")
  def appName:     String = configuration.get[String]("appName")
  def loginUrl:    String = configuration.get[String]("urls.login")
  def signOutUrl:  String = configuration.get[String]("urls.signOut")
  def ivUpliftUrl: String = configuration.get[String]("urls.ivUplift")
  def pegaPoeUrl:  String = configuration.get[String]("urls.pegaPoe")
  def timeout:     Int    = configuration.get[Int]("timeout-dialog.timeout")
  def countdown:   Int    = configuration.get[Int]("timeout-dialog.countdown")
  def cacheTtl:    Int    = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  def redirectToPEGA: Boolean =
    configuration.get[Boolean]("features.redirect-to-pega")
  def languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")
  def showOutageBanner: Boolean = configuration.get[Boolean]("features.showOutageBanner")

  def scaWrapperEnabled: Boolean =
    configuration.get[Boolean]("features.sca-wrapper-enabled")

  def ignoreHicbcCacheTTL: Duration =
    Duration(
      configuration.get[Long]("features.ignore-hicbc-check-cache.duration"),
      configuration.get[String]("features.ignore-hicbc-check-cache.timeunit")
    )

  private def contactHost = configuration.get[String]("contact-frontend.host")
  private def childBenefitServiceBaseUrl: String = servicesConfig.baseUrl("child-benefit-entitlement")
  private def exitSurveyBaseUrl:          String = configuration.get[String]("microservice.services.feedback-frontend.url")

  def childBenefitEntitlementUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/view-entitlements-and-payments"

  def changeOfBankUserInfoUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/change-bank-user-information"

  def getFtnaeAccountInfoUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/retrieve-ftnae-account-information"

  def updateFtnaeInfoUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/update-child-information/ftnae"

  def verifyBankAccountUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/verify-claimant-bank-account"

  def verifyBARNotLockedUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/verify-bar-not-locked"

  def updateBankAccountUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/update-claimant-bank-account"

  def dropChangeOfBankCacheUrl: String =
    s"$childBenefitServiceBaseUrl/child-benefit-service/drop-change-bank-cache"

  def exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/CHIB"

  def feedbackUrl: String = s"$contactHost/contact/beta-feedback?service=CHIB"

  def reportTechnicalProblemUrl(uri: String): String =
    s"$contactHost/contact/report-technical-problem?newTab=true&service=CHIB&referrerUrl=${RedirectUrl(contactHost + uri).get(UnsafePermitAll).encodedUrl}"

  def languageMap: Map[String, Lang] =
    Map("en" -> Lang("en"), "cy" -> Lang("cy"))

  def confidenceLevel: ConfidenceLevel =
    ConfidenceLevel
      .fromInt(configuration.get[Int]("confidenceLevel"))
      .getOrElse(ConfidenceLevel.L200)

  val protectTaxInfoUrl: String = getExternalUrl("tax-enrolment-assignment")

}
