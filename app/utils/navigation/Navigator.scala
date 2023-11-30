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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import models.cob.ConfirmNewAccountDetails.{No, Yes}
import models.ftnae.HowManyYears
import utils.pages._
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage, WhatTypeOfAccountPage}
import pages.ftnae.{HowManyYearsPage, LiveWithYouInUKPage, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage, WillYoungPersonBeStayingPage}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => (UserAnswers, Mode) => Call = {
    case WhatTypeOfAccountPage => (_, _) => controllers.cob.routes.NewAccountDetailsController.onPageLoad(NormalMode)
    case NewAccountDetailsPage =>
      (_, _) => controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
    case ConfirmNewAccountDetailsPage => (userAnswers, mode) => confirmAccountDetails(userAnswers, mode)
    case WhichYoungPersonPage         => (userAnswers, mode) => navigateWhichYoungPerson(userAnswers, mode)
    case WillYoungPersonBeStayingPage => (userAnswers, mode) => navigateWillYoungPersonBeStaying(userAnswers, mode)
    case SchoolOrCollegePage          => (userAnswers, mode) => navigateSchoolOrCollege(userAnswers, mode)
    case TwelveHoursAWeekPage         => (userAnswers, mode) => navigateTwelveHoursAWeek(userAnswers, mode)
    case HowManyYearsPage             => (userAnswers, mode) => navigateHowManyYears(userAnswers, mode)
    case WillCourseBeEmployerProvidedPage =>
      (userAnswers, mode) => navigateWillCourseBeEmployerProvided(userAnswers, mode)
    case LiveWithYouInUKPage => (userAnswers, mode) => navigateLiveWithYouIntheUK(userAnswers, mode)
    case _ @page =>
      (_, _) => {
        controllers.routes.ServiceUnavailableController.onPageLoad
      }
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case WhatTypeOfAccountPage => _ => controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(CheckMode)
    case NewAccountDetailsPage => _ => controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(CheckMode)
    case _                     => _ => controllers.ftnae.routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers, mode)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
  }

  private def navigateWhichYoungPerson(userAnswers: UserAnswers, mode: Mode): Call = {
    val YOUNG_PERSON_NOT_DISPLAYED_INDEX = "0"
//    val test: Option[String] = None

//    test match {
    userAnswers.get(WhichYoungPersonPage) match {
      case Some(YOUNG_PERSON_NOT_DISPLAYED_INDEX) =>
        controllers.ftnae.routes.WhyYoungPersonNotListedController.onPageLoad()
      case Some(_) => controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(mode).url))
        )
    }
  }
  private def navigateWillYoungPersonBeStaying(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(WillYoungPersonBeStayingPage) match {
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      case Some(true)  => controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(mode).url))
        )
    }
  private def navigateSchoolOrCollege(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(SchoolOrCollegePage) match {
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      case Some(true)  => controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(mode).url))
        )
    }
  private def navigateTwelveHoursAWeek(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(TwelveHoursAWeekPage) match {
      case Some(false) => controllers.ftnae.routes.NotEntitledController.onPageLoad()
      case Some(true)  => controllers.ftnae.routes.HowManyYearsController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(mode).url))
        )
    }
  private def navigateHowManyYears(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(HowManyYearsPage) match {
      case Some(HowManyYears.Other) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      case Some(_) =>
        controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.HowManyYearsController.onPageLoad(mode).url))
        )
    }
  private def navigateWillCourseBeEmployerProvided(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(WillCourseBeEmployerProvidedPage) match {
      case Some(true)  => controllers.ftnae.routes.NotEntitledCourseEmployerProvidedController.onPageLoad()
      case Some(false) => controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(mode).url))
        )
    }
  private def navigateLiveWithYouIntheUK(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(LiveWithYouInUKPage) match {
      case Some(true)  => controllers.ftnae.routes.CheckYourAnswersController.onPageLoad()
      case Some(false) => controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(mode).url))
        )
    }
  private def confirmAccountDetails(userAnswers: UserAnswers, mode: Mode): Call =
    userAnswers.get(ConfirmNewAccountDetailsPage) match {
      case Some(Yes) => controllers.cob.routes.AccountChangedController.onPageLoad()
      case Some(No)  => controllers.cob.routes.WhatTypeOfAccountController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.JourneyRecoveryController.onPageLoad(
          Some(RedirectUrl(controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(mode).url))
        )
    }
}
