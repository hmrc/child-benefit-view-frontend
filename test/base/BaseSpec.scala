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

package base

import generators.ModelGenerators
import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

trait BaseSpec
  extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with ScalaFutures
  with IntegrationPatience
  with MockitoSugar
  with ScalaCheckPropertyChecks
  with ModelGenerators {

  val userAnswersId: String = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())
  def messages(app: Application, request: RequestHeader): Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  protected implicit def hc(implicit rh: RequestHeader): HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)

  val withOrWithout = (bool: Boolean) => if (bool) "with" else "without"
  val isOrIsNot = (bool: Boolean) => if (bool) "is" else "is not"
}
