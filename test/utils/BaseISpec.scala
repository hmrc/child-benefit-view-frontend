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

package utils

import connectors.{ChildBenefitEntitlementConnector, DefaultChildBenefitEntitlementConnector}
import controllers.actions._
import models.UserAnswers
import org.jsoup.Jsoup
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Binding, bind}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

class BaseISpec extends WireMockSupport with GuiceOneAppPerSuite {
  override implicit lazy val app: Application = applicationBuilder().build()

  val userAnswersId: String = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def messages(app: Application, request: RequestHeader): Messages =
    app.injector.instanceOf[MessagesApi].preferred(request)

  protected implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)

  protected def applicationBuilder(
      config:      Map[String, Any] = Map(),
      userAnswers: Option[UserAnswers] = None,
      entitlementConnector: Binding[ChildBenefitEntitlementConnector] =
        bind[ChildBenefitEntitlementConnector].to[DefaultChildBenefitEntitlementConnector]
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        config ++ Map(
          "microservice.services.auth.port"                      -> wiremockPort,
          "microservice.services.child-benefit-entitlement.port" -> wiremockPort
        )
      )
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CBDataRetrievalAction].toInstance(new FakeCobDataRetrievalAction(userAnswers)),
        bind[VerifyBarNotLockedAction].toInstance(FakeVerifyBarNotLockedAction(verify = true)),
        bind[VerifyHICBCAction].toInstance(FakeVerifyHICBCAction(verify = true)),
        entitlementConnector
      )

  protected def applicationBuilderWithVerificationActions(
      config:      Map[String, Any] = Map(),
      userAnswers: Option[UserAnswers] = None,
      entitlementConnector: Binding[ChildBenefitEntitlementConnector] =
        bind[ChildBenefitEntitlementConnector].to[DefaultChildBenefitEntitlementConnector],
      verifyHICBCAction:        VerifyHICBCAction,
      verifyBarNotLockedAction: VerifyBarNotLockedAction
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(
        config ++ Map(
          "microservice.services.auth.port"                      -> wiremockPort,
          "microservice.services.child-benefit-entitlement.port" -> wiremockPort
        )
      )
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CBDataRetrievalAction].toInstance(new FakeCobDataRetrievalAction(userAnswers)),
        bind[VerifyBarNotLockedAction].toInstance(verifyBarNotLockedAction),
        bind[VerifyHICBCAction].toInstance(verifyHICBCAction),
        entitlementConnector
      )

  protected def assertSameHtmlAfter(
      transformation: String => String
  )(left:             String, right: String)(implicit position: Position): Assertion = {
    val leftHtml  = Jsoup.parse(transformation(left))
    val rightHtml = Jsoup.parse(transformation(right))
    leftHtml.html() mustBe rightHtml.html()
  }
}
