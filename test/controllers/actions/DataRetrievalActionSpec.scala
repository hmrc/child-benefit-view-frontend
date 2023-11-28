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

import base.BaseAppSpec
import models.UserAnswers
import models.common.NationalInsuranceNumber
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.SessionRepository
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends BaseAppSpec with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {
        userLoggedInChildBenefitUser(NinoUser)

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(None)
        val action = new Harness(sessionRepository)

        val result = action
          .callTransform(IdentifierRequest(FakeRequest(), NationalInsuranceNumber("QQ123456B"), true, "id"))
          .futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {
        userLoggedInChildBenefitUser(NinoUser)

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
        val action = new Harness(sessionRepository)

        val result = action
          .callTransform(IdentifierRequest(FakeRequest(), NationalInsuranceNumber("QQ123456C"), true, "id"))
          .futureValue

        result.userAnswers mustBe defined
      }
    }
  }
}
