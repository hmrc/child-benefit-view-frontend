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

package utils.navigation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import models.cob.ConfirmNewAccountDetails.{No, Yes}
import models.ftnae.{HowManyYears}
import utils.pages._
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import pages.ftnae.{HowManyYearsPage, LiveWithYouInUKPage, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage, WillYoungPersonBeStayingPage}
import play.api.Logging

@Singleton
class Navigator @Inject() () extends Logging {

  private val normalRoutes: Page => UserAnswers => Call = {
    case NewAccountDetailsPage            => _ => controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
    case ConfirmNewAccountDetailsPage     => userAnswers => confirmAccountDetails(userAnswers)
    case WhichYoungPersonPage             => userAnswers => navigateWhichYoungPerson(userAnswers)
    case WillYoungPersonBeStayingPage     => userAnswers => navigateWillYoungPersonBeStaying(userAnswers)
    case SchoolOrCollegePage              => userAnswers => navigateSchoolOrCollege(userAnswers)
    case TwelveHoursAWeekPage             => userAnswers => navigateTwelveHoursAWeek(userAnswers)
    case HowManyYearsPage                 => userAnswers => navigateHowManyYears(userAnswers)
    case WillCourseBeEmployerProvidedPage => userAnswers => navigateWillCourseBeEmployerProvided(userAnswers)
    case LiveWithYouInUKPage              => userAnswers => navigateLiveWithYouIntheUK(userAnswers)
    case _ @page =>
      _ => {
        logger.warn(s"reached state where page: $page is not implemented in Navigator.normalRoutes decision flow")
        controllers.routes.ServiceUnavailableController.onPageLoad
      }
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => controllers.ftnae.routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
  }

  private def navigateWhichYoungPerson(userAnswers: UserAnswers): Call = {
    val YOUNG_PERSON_NOT_DISPLAYED_INDEX = "0"

    userAnswers.get(WhichYoungPersonPage) match {
      case Some(YOUNG_PERSON_NOT_DISPLAYED_INDEX) =>
        controllers.ftnae.routes.WhyYoungPersonNotListedController.onPageLoad
      case Some(_) => controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode)
      case _       => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
  private def navigateWillYoungPersonBeStaying(userAnswers: UserAnswers): Call =
    userAnswers.get(WillYoungPersonBeStayingPage) match {
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      case Some(true)  => controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(NormalMode)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigateSchoolOrCollege(userAnswers: UserAnswers): Call =
    userAnswers.get(SchoolOrCollegePage) match {
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad
      case Some(true)  => controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(NormalMode)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigateTwelveHoursAWeek(userAnswers: UserAnswers): Call =
    userAnswers.get(TwelveHoursAWeekPage) match {
      case Some(false) => controllers.ftnae.routes.NotEntitledController.onPageLoad
      case Some(true)  => controllers.ftnae.routes.HowManyYearsController.onPageLoad(NormalMode)
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigateHowManyYears(userAnswers: UserAnswers): Call =
    userAnswers.get(HowManyYearsPage) match {
      case Some(HowManyYears.Other) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad
      case Some(_) =>
        controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode)
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigateWillCourseBeEmployerProvided(userAnswers: UserAnswers): Call =
    userAnswers.get(WillCourseBeEmployerProvidedPage) match {
      case Some(true)  => controllers.ftnae.routes.NotEntitledCourseEmployerProvidedController.onPageLoad
      case Some(false) => controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(NormalMode)
      case None        => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def navigateLiveWithYouIntheUK(userAnswers: UserAnswers): Call =
    userAnswers.get(LiveWithYouInUKPage) match {
      case Some(true)  => controllers.ftnae.routes.CheckYourAnswersController.onPageLoad
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad
      case _           => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  private def confirmAccountDetails(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfirmNewAccountDetailsPage) match {
      case Some(Yes) => controllers.cob.routes.AccountChangedController.onPageLoad()
      case Some(No)  => controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode)
      case _         => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
}
