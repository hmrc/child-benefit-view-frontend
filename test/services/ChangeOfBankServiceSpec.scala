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

package services

import cats.implicits.catsSyntaxEitherId
import connectors.ChangeOfBankConnector
import generators.ModelGenerators
import models.{CBEnvelope, UserAnswers}
import models.cob.{NewAccountDetails, UpdateBankAccountRequest, UpdateBankDetailsResponse}
import models.common.NationalInsuranceNumber
import models.errors.CBError
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ChangeOfBankServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures with ModelGenerators {
  val cobConnector          = mock[ChangeOfBankConnector]
  val sessionRepository     = mock[SessionRepository]
  implicit val auditService = mock[AuditService]
  implicit val ec           = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc           = HeaderCarrier()

  val sut = new ChangeOfBankService(cobConnector, sessionRepository)

  "ChangeOfBankService" should {
    "submitClaimantChangeOfBank" should {
      "GIVEN a valid NewAccountDetails, Id and UserAnwers" should {
        "WHEN the ChangeOfBankConnector returns a successful response" should {
          val expectedResponse = UpdateBankDetailsResponse("UNIT_TEST_STATUS")
          when(cobConnector.updateBankAccount(any[UpdateBankAccountRequest])(any[ExecutionContext], any[HeaderCarrier]))
            .thenReturn(CBEnvelope(expectedResponse))
          when(sessionRepository.clear(any[String]))
            .thenReturn(Future.successful(true))

          forAll((arbitrary[NewAccountDetails], "accountDetails"), (arbitrary[String](generateId), "id")) {
            (accountDetails, id) =>
              val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), id, NationalInsuranceNumber(id), UserAnswers(id))
              whenReady(sut.submitClaimantChangeOfBank(Some(accountDetails), request)(ec, hc).value) { response =>
                s"Id: $id" should {
                  s"THEN the expected UpdateBankDetailsResponse is returned - Id: $id" in {
                    response mustBe expectedResponse.asRight[CBError]
                  }
                  s"AND a call is made to clear the user's session - Id: $id" in {
                    verify(sessionRepository).clear(id)
                  }
                }
              }
          }
        }
      }
    }
  }
}
