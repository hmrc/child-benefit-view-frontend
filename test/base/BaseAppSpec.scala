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

package base

import controllers.actions._
import models.UserAnswers
import org.jsoup.Jsoup
import org.scalactic.source.Position
import org.scalatest.Assertion
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

trait BaseAppSpec extends BaseSpec with WireMockSupport {
  protected def applicationBuilder(
      config:      Map[String, Any] = Map(),
      userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(defaultWireMockConfig ++ config)
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[CBDataRetrievalAction].toInstance(new FakeCobDataRetrievalAction(userAnswers))
      )

  protected def applicationBuilderWithVerificationActions(
      config:                   Map[String, Any] = Map(),
      userAnswers:              Option[UserAnswers] = None,
      verifyBarNotLockedAction: VerifyBarNotLockedAction = FakeVerifyBarNotLockedAction(verify = true),
      verifyHICBCAction:        VerifyHICBCAction = FakeVerifyHICBCAction(verify = true)
  ): GuiceApplicationBuilder = {
    applicationBuilder(config, userAnswers)
      .overrides(
        bind[VerifyBarNotLockedAction].toInstance(verifyBarNotLockedAction),
        bind[VerifyHICBCAction].toInstance(verifyHICBCAction)
      )
  }

  protected def assertSameHtmlAfter(
      transformation: String => String
  )(left:             String, right: String)(implicit position: Position): Assertion = {
    val leftHtml  = Jsoup.parse(transformation(left))
    val rightHtml = Jsoup.parse(transformation(right))
    leftHtml.html() mustBe rightHtml.html()
  }

}
