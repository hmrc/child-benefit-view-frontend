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

package utils

import connectors.{ChildBenefitEntitlementConnector, MockChildBenefitEntitlementConnector}
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, FakeDataRetrievalAction}
import models.UserAnswers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import play.api.inject.guice.GuiceApplicationBuilder
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
      config:      Map[String, Any] = Map("microservice.services.auth.port" -> wiremockPort),
      userAnswers: Option[UserAnswers] = None,
      entitlementConnector: Binding[ChildBenefitEntitlementConnector] =
        bind[ChildBenefitEntitlementConnector].to[MockChildBenefitEntitlementConnector]
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(config)
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        entitlementConnector
      )
}
