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

package controllers.ftnae

import forms.ftnae.WhichYoungPersonFormProvider
import models.common.{FirstForename, Surname}
import models.ftnae.{Crn, FtneaChildInfo, FtneaClaimantInfo, FtneaResponse}
import models.{CheckMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtneaResponseUserAnswer, WhichYoungPersonPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.Aliases.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ftnae.WhichYoungPersonView

import scala.concurrent.Future

class WhichYoungPersonControllerSpec extends BaseISpec with MockitoSugar with FtneaFixture {

  def onwardRoute = Call("GET", "/foo")

  lazy val whichYoungPersonRoute = controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(CheckMode).url

  val formProvider             = new WhichYoungPersonFormProvider()
  val form                     = formProvider()
  lazy val extendPaymentsRoute = controllers.ftnae.routes.ExtendPaymentsController.onPageLoad.url

  private def arrangeRadioButtons(
      ftneaResponseUserAnswer: FtneaResponse
  ): List[RadioItem] = {
    val initialOrder: List[(String, Int)] = ("Child Not Listed" :: (
      ftneaResponseUserAnswer.children
        .map(c => s"${c.name.value} ${c.lastName.value}")
      )).zipWithIndex.toList

    val (childNotListedMessage :: restOfTheList) = initialOrder

    val orderedWithIndex0InTheEnd = restOfTheList ::: List(childNotListedMessage)
    orderedWithIndex0InTheEnd.map(x =>
      RadioItem(content = Text(x._1), value = Some(x._2.toString), id = Some(s"value_${x._2}"))
    )

  }

  val ftneaResponse = FtneaResponse(
    FtneaClaimantInfo(FirstForename("s"), Surname("sa")),
    List(
      FtneaChildInfo(
        Crn("crn1234"),
        FirstForename("First Name"),
        None,
        Surname("Surname"),
        sixteenBy1stOfSeptemberThisYear,
        getFirstMondayOfSeptemberThisYear()
      )
    )
  )

  val mockSessionRepository = mock[SessionRepository]
  "WhichYoungPerson Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(FtneaResponseUserAnswer, ftneaResponse)
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      userLoggedInChildBenefitUser(NinoUser)

      running(application) {
        val request = FakeRequest(GET, whichYoungPersonRoute)
          .withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhichYoungPersonView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form, CheckMode, arrangeRadioButtons(ftneaResponse), ftneaResponse)(
            request,
            messages(application)
          ).toString
        )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, 1)
        .flatMap(x => x.set(FtneaResponseUserAnswer, ftneaResponse))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      running(application) {
        val request = FakeRequest(GET, whichYoungPersonRoute)

        val view = application.injector.instanceOf[WhichYoungPersonView]

        val result = route(application, request).value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(form.fill(1), CheckMode, arrangeRadioButtons(ftneaResponse), ftneaResponse)(
            request,
            messages(application)
          ).toString
        )
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, 1)
        .flatMap(x => x.set(FtneaResponseUserAnswer, ftneaResponse))
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
      when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whichYoungPersonRoute)
            .withFormUrlEncodedBody(("value", "1"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = UserAnswers(userAnswersId)
        .set(FtneaResponseUserAnswer, ftneaResponse)
        .success
        .value

      when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()
      running(application) {
        val request =
          FakeRequest(POST, whichYoungPersonRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhichYoungPersonView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        assertSameHtmlAfter(removeCsrfAndNonce)(
          contentAsString(result),
          view(boundForm, CheckMode, arrangeRadioButtons(ftneaResponse), ftneaResponse)(
            request,
            messages(application)
          ).toString
        )
      }
    }

  }
}
