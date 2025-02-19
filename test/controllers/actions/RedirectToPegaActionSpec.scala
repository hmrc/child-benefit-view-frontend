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

package controllers.actions

import base.BaseAppSpec
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{DefaultMessagesApi, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, MessagesRequest, Result}
import play.api.test.FakeRequest
import services.ChangeBankAccountRedirectService
import uk.gov.hmrc.http.HttpVerbs.GET

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RedirectToPegaActionSpec extends BaseAppSpec with MockitoSugar {

  "when RedirectToPega is false the action" - {
    "must move on with the request (open the gate) and return None" in new Setup(redirectToPega = false) {
      val result: Option[Result] = action.callFilter(messagesRequest).futureValue
      result must equal(None)
    }
  }

  "when RedirectToPega is true the action" - {
    "must NOT move on with the request (close the gate) and redirect to Pega" in new Setup(redirectToPega = true) {
      val result: Option[Result] = action.callFilter(messagesRequest).futureValue
      result must equal(Some(Redirect("https://account.hmrc.gov.uk/child-benefit/make_a_claim/change-of-bank")))
    }
  }

  class Setup(redirectToPega: Boolean) {
    class Harness(
        changeBankAccountRedirectService: ChangeBankAccountRedirectService,
        appConfig:                        FrontendAppConfig
    ) extends RedirectToPegaActionImpl(changeBankAccountRedirectService, appConfig) {
      def callFilter[A](request: MessagesRequest[A]): Future[Option[Result]] = this.filter(request)
    }

    val messagesApi:     MessagesApi                             = new DefaultMessagesApi(Map.empty[String, Map[String, String]])
    val fakeRequest:     FakeRequest[AnyContentAsEmpty.type]     = FakeRequest(GET, "")
    val messagesRequest: MessagesRequest[AnyContentAsEmpty.type] = new MessagesRequest(fakeRequest, messagesApi)

    val mockChangeBankAccountRedirectService: ChangeBankAccountRedirectService = mock[ChangeBankAccountRedirectService]
    when(mockChangeBankAccountRedirectService.getChangeBankAccountRedirectToggle).thenReturn(Future.successful(
      redirectToPega
    ))

    val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
    when(mockAppConfig.pegaCobUrl).thenReturn("https://account.hmrc.gov.uk/child-benefit/make_a_claim/change-of-bank")

    val action: Harness = new Harness(mockChangeBankAccountRedirectService, mockAppConfig)
  }
}
