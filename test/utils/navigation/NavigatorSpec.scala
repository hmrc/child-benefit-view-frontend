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

package utils.navigation

import base.SpecBase
import controllers.routes
import models.cob.ConfirmNewAccountDetails.Yes
import utils.pages._
import models._
import models.cob.ConfirmNewAccountDetails._
import models.cob.NewAccountDetails
import models.ftnae.{HowManyYears, WhichYoungPerson}
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import pages.ftnae.{HowManyYearsPage, LiveWithYouInUKPage, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage, WillYoungPersonBeStayingPage}
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to service down page" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          NormalMode,
          UserAnswers("id")
        ) mustBe routes.ServiceUnavailableController.onPageLoad
      }

      "must go from NewAccountDetails to ConfirmNewAccountDetails page" in {
        navigator.nextPage(
          NewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(NewAccountDetails("Name", "123456", "00000000001")))
        ) mustBe controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
      }

      "must go from ConfirmNewAccountDetails to AccountChanged page" in {
        navigator.nextPage(
          ConfirmNewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(NewAccountDetails("Name", "123456", "00000000001")))
            .set(ConfirmNewAccountDetailsPage, Yes)
            .get
        ) mustBe controllers.cob.routes.AccountChangedController.onPageLoad()
      }

      "must go from ConfirmNewAccountDetails back to NewAccountDetails page when No is selected" in {
        navigator.nextPage(
          ConfirmNewAccountDetailsPage,
          NormalMode,
          UserAnswers("id", Json.toJsObject(NewAccountDetails("Name", "123456", "00000000001")))
            .set(ConfirmNewAccountDetailsPage, No)
            .get
        ) mustBe controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode)
      }

      val emptyUserAnswers = UserAnswers("id")
      val allAnsweredForFtnae = for {
        fa  <- emptyUserAnswers.set(WhichYoungPersonPage, WhichYoungPerson.values.head)
        sa  <- fa.set(WillYoungPersonBeStayingPage, true)
        ta  <- sa.set(SchoolOrCollegePage, true)
        fa  <- ta.set(TwelveHoursAWeekPage, true)
        fia <- fa.set(HowManyYearsPage, HowManyYears.Twoyears)
        sa  <- fia.set(WillCourseBeEmployerProvidedPage, false)
        sea <- sa.set(LiveWithYouInUKPage, true)
      } yield sea

      "must go from WhichYoungPersonPage to WillYoungPersonBeStayingPage" in {

        navigator.nextPage(
          WhichYoungPersonPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode)
      }

      "must go from WillYoungPersonBeStayingPage to SchoolOrCollegePage " in {
        navigator.nextPage(
          WillYoungPersonBeStayingPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(NormalMode)
      }

      "must go from SchoolOrCollegePage to TwelveHoursAWeekPage " in {
        navigator.nextPage(
          SchoolOrCollegePage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(NormalMode)
      }

      "must go from TwelveHoursAWeekPage to HowManyYearsPage " in {
        navigator.nextPage(
          TwelveHoursAWeekPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.HowManyYearsController.onPageLoad(NormalMode)
      }

      "must go from HowManyYearsPage to WillCourseBeEmployerProvidedPage " in {
        navigator.nextPage(
          HowManyYearsPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode)
      }

      "must go from WillCourseBeEmployerProvidedPage to LiveWithYouInUKPage" in {
        navigator.nextPage(
          WillCourseBeEmployerProvidedPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(NormalMode)
      }

      "must go from LiveWithYouInUKPage to CheckYourAnswersPage" in {
        navigator.nextPage(
          LiveWithYouInUKPage,
          NormalMode,
          allAnsweredForFtnae.success.value
        ) mustBe controllers.ftnae.routes.CheckYourAnswersController.onPageLoad
      }

      "must be kicked out from WhichYoungPersonPage to WhyYoungPersonNotListedPage" in {
        navigator.nextPage(
          WhichYoungPersonPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(WhichYoungPersonPage, WhichYoungPerson.ChildNotListed)).success.value
        ) mustBe controllers.ftnae.routes.WhyYoungPersonNotListedController.onPageLoad()
      }

      "must be kicked out from WillYoungPersonBeStayingPage to UseDifferentFormPage" in {
        navigator.nextPage(
          WillYoungPersonBeStayingPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(WillYoungPersonBeStayingPage, false)).success.value
        ) mustBe controllers.ftnae.routes.NotEntitledController.onPageLoad()
      }

      "must be kicked out from SchoolOrCollegePage to UseDifferentFormPage" in {
        navigator.nextPage(
          SchoolOrCollegePage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(SchoolOrCollegePage, false)).success.value
        ) mustBe controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      }

      "must be kicked out from TwelveHoursAWeekPage to NotEntitledPage" in {
        navigator.nextPage(
          TwelveHoursAWeekPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(TwelveHoursAWeekPage, false)).success.value
        ) mustBe controllers.ftnae.routes.NotEntitledController.onPageLoad()
      }

      "must be kicked out from HowManyYearsPage to UseDifferentFormPage" in {
        navigator.nextPage(
          HowManyYearsPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(HowManyYearsPage, HowManyYears.Other)).success.value
        ) mustBe controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      }

      "must be kicked out from LiveWithYouInUKPage to UseDifferentFormPage" in {
        navigator.nextPage(
          LiveWithYouInUKPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(LiveWithYouInUKPage, false)).success.value
        ) mustBe controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      }

      "must be kicked out from WillCourseBeEmployerProvidedPage to " in {
        navigator.nextPage(
          WillCourseBeEmployerProvidedPage,
          NormalMode,
          allAnsweredForFtnae.flatMap(_.set(WillCourseBeEmployerProvidedPage, true)).success.value
        ) mustBe controllers.ftnae.routes.NotEntitledCourseEmployerProvidedController.onPageLoad()
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.ftnae.routes.CheckYourAnswersController.onPageLoad
      }
    }
  }
}
