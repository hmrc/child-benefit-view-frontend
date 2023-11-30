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

import models.common.NationalInsuranceNumber
import models.ftnae.HowManyYears
import models.requests.{BaseDataRequest, DataRequest, OptionalDataRequest, FtnaePaymentsExtendedPageDataRequest}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae._
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.FtnaePaymentsExtendedPageSessionRepository
import utils.BaseISpec
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FtnaePaymentsExtendedPageDataRequiredActionSpec extends BaseISpec with MockitoSugar {

  class Harness(ftnaePaymentsExtendedPageSessionRepository: FtnaePaymentsExtendedPageSessionRepository)
      extends FtnaePaymentsExtendedPageDataRequiredActionImpl(ftnaePaymentsExtendedPageSessionRepository) {
    def callTransform[A](request: OptionalDataRequest[A]): Future[Either[Result, BaseDataRequest[A]]] = refine(request)
  }
  private val nino = NationalInsuranceNumber("QQ123456C")

  private val allAnsweredForFtnae = for {
    fa  <- emptyUserAnswers.set(WhichYoungPersonPage, "John Doe")
    sa  <- fa.set(WillYoungPersonBeStayingPage, true)
    ta  <- sa.set(SchoolOrCollegePage, true)
    fa  <- ta.set(TwelveHoursAWeekPage, true)
    fia <- fa.set(HowManyYearsPage, HowManyYears.Twoyears)
    sa  <- fia.set(WillCourseBeEmployerProvidedPage, false)
    sea <- sa.set(LiveWithYouInUKPage, true)
  } yield sea

  val fakeRequest = FakeRequest()

  "FtnaePaymentsExtendedPage Data Required Action" - {

    "when there is no data in the cache" - {

      "must check FtnaePaymentsExtendedPage session repository userAnswers to 'None' in the request" in {
        userLoggedInChildBenefitUser(NinoUser)

        val ftnaePaymentsExtendedPageSessionRepository = mock[FtnaePaymentsExtendedPageSessionRepository]
        when(ftnaePaymentsExtendedPageSessionRepository.get("id")) thenReturn Future(None)
        val action = new Harness(ftnaePaymentsExtendedPageSessionRepository)

        val result = action
          .callTransform(OptionalDataRequest(fakeRequest, "id", nino, None))
          .futureValue
        result mustBe Left(Redirect(controllers.routes.ServiceUnavailableController.onPageLoad.url))
      }
    }

    "when there is already data in the request coming from the pre chained request data action" - {

      "must NOT call ftnaePaymentsExtendedPageSessionRepository and pass the userAnswers object over to the data required request" in {

        userLoggedInChildBenefitUser(NinoUser)

        val ftnaePaymentsExtendedPageSessionRepository = mock[FtnaePaymentsExtendedPageSessionRepository]

        verify(ftnaePaymentsExtendedPageSessionRepository, times(0)).get("id")

        val action = new Harness(ftnaePaymentsExtendedPageSessionRepository)

        val result = action
          .callTransform(OptionalDataRequest(fakeRequest, "id", nino, Some(allAnsweredForFtnae.get)))
          .futureValue

        result mustBe Right(DataRequest(fakeRequest, "id", nino, allAnsweredForFtnae.get))
      }
    }

    "when there is no data in the request coming from the pre chained request data action(due to session data clearance on the payments extended page)" - {

      "must build a userAnswers object and add it to the request" in {

        userLoggedInChildBenefitUser(NinoUser)

        val ftnaePaymentsExtendedPageSessionRepository = mock[FtnaePaymentsExtendedPageSessionRepository]
        when(ftnaePaymentsExtendedPageSessionRepository.get("id")) thenReturn Future(Some(allAnsweredForFtnae.get))
        val action = new Harness(ftnaePaymentsExtendedPageSessionRepository)

        val result = action
          .callTransform(OptionalDataRequest(fakeRequest, "id", nino, None))
          .futureValue

        val temporaryDataRequest =
          FtnaePaymentsExtendedPageDataRequest[AnyContent](fakeRequest, "id", nino, allAnsweredForFtnae.get)
        result mustBe Right(temporaryDataRequest)
      }
    }
  }
}
