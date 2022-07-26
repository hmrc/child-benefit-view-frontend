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

package controllers.cob

import forms.cob.NewAccountDetailsFormProvider
import models.cob.NewAccountDetails
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.cob.NewAccountDetailsPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import testconfig.TestConfig
import testconfig.TestConfig._
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ErrorTemplate
import views.html.cob.NewAccountDetailsView

import scala.concurrent.Future

class NewAccountDetailsControllerSpec extends BaseISpec with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new NewAccountDetailsFormProvider()
  val form         = formProvider()

  lazy val newAccountDetailsRoute = controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode).url
  val newAccountDetails           = NewAccountDetails("name", "123456", "11110000")
  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      NewAccountDetailsPage.toString -> Json.toJsObject(newAccountDetails)
    )
  )

  "NewAccountDetails Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and the correct view for a GET" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[NewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(form, NormalMode)(request, messages(application)).toString
          )
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        userLoggedInChildBenefitUser(NinoUser)

        val mockSessionRepository = mock[SessionRepository]
        val application = applicationBuilder(config, userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val view = application.injector.instanceOf[NewAccountDetailsView]
          when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
          val result = route(application, request).value

          status(result) mustEqual OK

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(form.fill(newAccountDetails), NormalMode)(
              request,
              messages(application)
            ).toString
          )
        }
      }

      "must redirect to the next page when valid data is submitted" in {
        userLoggedInChildBenefitUser(NinoUser)

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(config, userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, newAccountDetailsRoute)
              .withFormUrlEncodedBody(
                ("newAccountHoldersName", newAccountDetails.newAccountHoldersName),
                ("newSortCode", newAccountDetails.newSortCode),
                ("newAccountNumber", newAccountDetails.newAccountNumber)
              )
              .withSession("authToken" -> "Bearer 123")

          when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))
          when(mockSessionRepository.set(userAnswers)) thenReturn Future.successful(true)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, newAccountDetailsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))
              .withSession("authToken" -> "Bearer 123")

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[NewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(boundForm, NormalMode)(request, messages(application)).toString
          )
        }
      }

      "must return OK for a GET if no existing data is found" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual 200
        }
      }

      "must return BAD_REQUEST for a POST if no existing data is found" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, newAccountDetailsRoute).withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
    "when the change of bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, newAccountDetailsRoute)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorTemplate]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
              request,
              messages(application)
            ).toString
          )
        }
      }
    }
  }
}
