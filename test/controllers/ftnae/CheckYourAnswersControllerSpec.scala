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

import models.ftnae.{HowManyYears, WhichYoungPerson}
import models.viewmodels.checkAnswers._
import models.viewmodels.govuk.SummaryListFluency
import models.{NormalMode, UserAnswers}
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.ftnae._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testconfig.TestConfig
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.NinoUser
import views.html.ftnae.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends BaseISpec with SummaryListFluency with TableDrivenPropertyChecks {
  private val allAnsweredForFtnae = for {
    fa  <- emptyUserAnswers.set(WhichYoungPersonPage, WhichYoungPerson.values.head)
    sa  <- fa.set(WillYoungPersonBeStayingPage, true)
    ta  <- sa.set(SchoolOrCollegePage, true)
    fa  <- ta.set(TwelveHoursAWeekPage, true)
    fia <- fa.set(HowManyYearsPage, HowManyYears.Twoyears)
    sa  <- fia.set(WillCourseBeEmployerProvidedPage, false)
    sea <- sa.set(LiveWithYouInUKPage, true)
  } yield sea

  "Check Your Answers Controller" - {

    val config = TestConfig()

    "must return OK and the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(config, userAnswers = Some(allAnsweredForFtnae.success.value))
        .configure(
          "microservice.services.auth.port" -> wiremockPort
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.CheckYourAnswersController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value

        val view          = application.injector.instanceOf[CheckYourAnswersView]
        val userAnswers   = allAnsweredForFtnae.success.value
        implicit val msgs = messages(application)
        val summaryRows = for {
          whichYoungPersonRow             <- WhichYoungPersonSummary.row(userAnswers)
          willYoungPersonBeStayingRow     <- WillYoungPersonBeStayingSummary.row(userAnswers)
          schoolOrCollegeRow              <- SchoolOrCollegeSummary.row(userAnswers)
          twelveHoursAWeekRow             <- TwelveHoursAWeekSummary.row(userAnswers)
          howManyYearsRow                 <- HowManyYearsSummary.row(userAnswers)
          willCourseBeEmployerProvidedRow <- WillCourseBeEmployerProvidedSummary.row(userAnswers)
          liveWithYouInUKRow              <- LiveWithYouInUKSummary.row(userAnswers)
        } yield List(
          whichYoungPersonRow,
          willYoungPersonBeStayingRow,
          schoolOrCollegeRow,
          twelveHoursAWeekRow,
          howManyYearsRow,
          willCourseBeEmployerProvidedRow,
          liveWithYouInUKRow
        )
        val list = SummaryListViewModel(
          summaryRows.getOrElse(List.empty)
        )

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(contentAsString(result), view(list)(request, messages(application)).toString)
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(config, userAnswers = None).configure().build()

      running(application) {
        val request = FakeRequest(GET, controllers.ftnae.routes.CheckYourAnswersController.onPageLoad.url)
          .withSession(("authToken", "Bearer 123"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to first unanswered page" in {
      val firstAnswered          = emptyUserAnswers.set(WhichYoungPersonPage, WhichYoungPerson.values.head)
      val firstMissingTillSecond = emptyUserAnswers.set(WillYoungPersonBeStayingPage, true)
      val secondMissingTillThird = firstAnswered.flatMap(_.set(SchoolOrCollegePage, true))

      secondMissingTillThird.flatMap(
        _.set(WillYoungPersonBeStayingPage, true).flatMap(_.set(TwelveHoursAWeekPage, true))
      )

      val scenarios = Table(
        ("userAnswers", "redirectUrl"),
        (
          firstAnswered.success.value,
          controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode).url
        ),
        (
          firstMissingTillSecond.success.value,
          controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(NormalMode).url
        ),
        (
          secondMissingTillThird.success.value,
          controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode).url
        )
      )

      userLoggedInChildBenefitUser(NinoUser)

      forAll(scenarios) { (userAnswers: UserAnswers, url: String) =>
        {
          val application = applicationBuilder(config, userAnswers = Some(userAnswers)).configure().build()

          running(application) {
            val request = FakeRequest(GET, controllers.ftnae.routes.CheckYourAnswersController.onPageLoad.url)
              .withSession(("authToken", "Bearer 123"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(
              result
            ).value mustEqual url
          }
        }
      }
    }

    "must redirect to first kickout page" in {
      val secondAnswerWrong = allAnsweredForFtnae.flatMap(_.set(WillYoungPersonBeStayingPage, false))

      val scenarios = Table(
        ("userAnswers", "redirectUrl"),
        (
          secondAnswerWrong.success.value,
          controllers.ftnae.routes.UseDifferentFormController.onPageLoad().url
        )
      )

      userLoggedInChildBenefitUser(NinoUser)

      forAll(scenarios) { (userAnswers: UserAnswers, url: String) =>
        {
          val application = applicationBuilder(config, userAnswers = Some(userAnswers)).configure().build()

          running(application) {
            val request = FakeRequest(GET, controllers.ftnae.routes.CheckYourAnswersController.onPageLoad.url)
              .withSession(("authToken", "Bearer 123"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(
              result
            ).value mustEqual url
          }
        }
      }
    }
  }
}
