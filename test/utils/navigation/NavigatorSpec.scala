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

import base.BaseAppSpec
import controllers.cob.{routes => cobroutes}
import controllers.ftnae.{routes => ftnaeroutes}
import controllers.routes
import models._
import models.cob.ConfirmNewAccountDetails._
import models.cob._
import models.ftnae.HowManyYears
import pages.cob._
import pages.ftnae._
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import utils.pages._

import scala.util.Try

class NavigatorSpec extends BaseAppSpec {

  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]

  val app: Application = applicationBuilder().build()
  val navigator: Navigator = app.injector.instanceOf[Navigator]

  def fromOnePageToNextTest(
      fromPage:    Page,
      toPageName:  String,
      toCall:      Call,
      userAnswers: UserAnswers,
      addendum:    Option[String],
      mode:        Mode
  ): Unit = {
    s"must go from the ${fromPage.toString} page to the $toPageName page${addendum.fold("")(a => s" $a")}" in {
      navigator.nextPage(
        fromPage,
        mode,
        userAnswers
      ) mustBe toCall
    }
  }

  val defaultUA: UserAnswers                 = UserAnswers("id")
  val newAccountDetailsUA: UserAnswers       = UserAnswers("id", Json.toJsObject(NewAccountDetails("Name", "123456", "00000000001")))
  val confirmedAccountDetailsUA: UserAnswers = newAccountDetailsUA.set(ConfirmNewAccountDetailsPage, Yes).get
  val rejectedAccountDetailsUA: UserAnswers  = newAccountDetailsUA.set(ConfirmNewAccountDetailsPage, No).get

  val happyPathFTNAECreator: Try[UserAnswers] = for {
    a <- defaultUA.set(WhichYoungPersonPage, "John Doe")
    b <- a.set(WillYoungPersonBeStayingPage, true)
    c <- b.set(SchoolOrCollegePage, true)
    d <- c.set(TwelveHoursAWeekPage, true)
    e <- d.set(HowManyYearsPage, HowManyYears.Twoyears)
    f <- e.set(WillCourseBeEmployerProvidedPage, false)
    g <- f.set(LiveWithYouInUKPage, true)
  } yield g
  val happyPathFTNAEUA: UserAnswers = happyPathFTNAECreator.success.value

  val kickOutPathFTNAECreator: Try[UserAnswers] = for {
    a <- defaultUA.set(WhichYoungPersonPage, "0")
    b <- a.set(WillYoungPersonBeStayingPage, false)
    c <- b.set(SchoolOrCollegePage, false)
    d <- c.set(TwelveHoursAWeekPage, false)
    e <- d.set(HowManyYearsPage, HowManyYears.Other)
    f <- e.set(WillCourseBeEmployerProvidedPage, true)
    g <- f.set(LiveWithYouInUKPage, false)
  } yield g
  val kickOutPathFTNAEUA: UserAnswers = kickOutPathFTNAECreator.success.value

  val selected: String => String        = (select: String) => s"when $select is selected"
  val kickOut: String => String         = (message: String) => s"$message (Kick Out)"
  val selectedKickOut: String => String = (select: String) => kickOut(selected(select))

  val recovery: Option[String]           = Some("when no valid value is selected")
  val yesSelected: Option[String]        = Some(selected("Yes"))
  val yesSelectedKickOut: Option[String] = Some(selectedKickOut(yesSelected.get))
  val noSelected: Option[String]         = Some(selected("No"))
  val noSelectedKickOut: Option[String]  = Some(selectedKickOut(noSelected.get))

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

      "Change of Bank" - {
        val normalModeCoBTestCases = Table(
          ("Page to start from", "Expected page", "User Answers override", "Test name addendum", "Expected Call"),
          (
            WhatTypeOfAccountPage,
            "newAccountDetails",
            defaultUA,
            None,
            cobroutes.NewAccountDetailsController.onPageLoad(NormalMode)
          ),
          (
            NewAccountDetailsPage,
            "confirmNewAccountDetails",
            newAccountDetailsUA,
            None,
            cobroutes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode)
          ),
          (
            ConfirmNewAccountDetailsPage,
            "accountChanged",
            confirmedAccountDetailsUA,
            yesSelected,
            cobroutes.AccountChangedController.onPageLoad()
          ),
          (
            ConfirmNewAccountDetailsPage,
            "whatTypeofAccount",
            rejectedAccountDetailsUA,
            noSelected,
            cobroutes.WhatTypeOfAccountController.onPageLoad(NormalMode)
          )
        )

        forAll(normalModeCoBTestCases) {
          (fromPage, toPageName, userAnswersOverride, addendum: Option[String], toCall) =>
            fromOnePageToNextTest(fromPage, toPageName, toCall, userAnswersOverride, addendum, NormalMode)
        }
      }

      "FTNAE" - {
        "Happy Path" - {
          val normalModeFTNAETestCases = Table(
            ("Page to start from", "Expected page", "User Answers override", "Test name addendum", "Expected Call"),
            (
              WhichYoungPersonPage,
              "willYoungPersonBeStaying",
              happyPathFTNAEUA,
              Some(s"when a child's name is selected"),
              ftnaeroutes.WillYoungPersonBeStayingController.onPageLoad(NormalMode)
            ),
            (
              WillYoungPersonBeStayingPage,
              "schoolOrCollegePage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.SchoolOrCollegeController.onPageLoad(NormalMode)
            ),
            (
              SchoolOrCollegePage,
              "twelveHoursAWeekPage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.TwelveHoursAWeekController.onPageLoad(NormalMode)
            ),
            (
              TwelveHoursAWeekPage,
              "howManyYearsPage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.HowManyYearsController.onPageLoad(NormalMode)
            ),
            (
              HowManyYearsPage,
              "willCourseBeEmployerProvidedPage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode)
            ),
            (
              WillCourseBeEmployerProvidedPage,
              "liveWithYouInUKPage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.LiveWithYouInUKController.onPageLoad(NormalMode)
            ),
            (
              LiveWithYouInUKPage,
              "checkYourAnswersPage",
              happyPathFTNAEUA,
              yesSelected,
              ftnaeroutes.CheckYourAnswersController.onPageLoad()
            )
          )

          forAll(normalModeFTNAETestCases) {
            (fromPage, toPageName, userAnswersOverride, addendum: Option[String], toCall) =>
              fromOnePageToNextTest(fromPage, toPageName, toCall, userAnswersOverride, addendum, NormalMode)
          }
        }

        "Kick Out Pages" - {
          val normalModeFTNAEKickOutTestCases = Table(
            ("Page to start from", "Expected page", "User Answers override", "Test name addendum", "Expected Call"),
            (
              WhichYoungPersonPage,
              "whyYoungPersonNotListedPage",
              kickOutPathFTNAEUA,
              Some(selectedKickOut("Young Person not listed")),
              ftnaeroutes.WhyYoungPersonNotListedController.onPageLoad()
            ),
            (
              WillYoungPersonBeStayingPage,
              "useDifferentFormPage",
              kickOutPathFTNAEUA,
              noSelectedKickOut,
              ftnaeroutes.UseDifferentFormController.onPageLoad()
            ),
            (
              SchoolOrCollegePage,
              "useDifferentFormPage",
              kickOutPathFTNAEUA,
              noSelectedKickOut,
              ftnaeroutes.UseDifferentFormController.onPageLoad()
            ),
            (
              TwelveHoursAWeekPage,
              "notEntitledPage",
              kickOutPathFTNAEUA,
              noSelectedKickOut,
              ftnaeroutes.NotEntitledController.onPageLoad()
            ),
            (
              HowManyYearsPage,
              "useDifferentFormPage",
              kickOutPathFTNAEUA,
              Some(selected("Other")),
              ftnaeroutes.UseDifferentFormController.onPageLoad()
            ),
            (
              LiveWithYouInUKPage,
              "useDifferentFormPage",
              kickOutPathFTNAEUA,
              noSelectedKickOut,
              ftnaeroutes.UseDifferentFormController.onPageLoad()
            ),
            (
              WillCourseBeEmployerProvidedPage,
              "useDifferentFormPage",
              kickOutPathFTNAEUA,
              yesSelectedKickOut,
              ftnaeroutes.NotEntitledCourseEmployerProvidedController.onPageLoad()
            )
          )
          forAll(normalModeFTNAEKickOutTestCases) {
            (fromPage, toPageName, userAnswersOverride, addendum: Option[String], toCall) =>
              fromOnePageToNextTest(fromPage, toPageName, toCall, userAnswersOverride, addendum, NormalMode)
          }
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(
          UnknownPage,
          CheckMode,
          UserAnswers("id")
        ) mustBe controllers.ftnae.routes.CheckYourAnswersController.onPageLoad()
      }

      "Change of Bank" - {
        val checkModeModeFTNAECoBTable = Table(
          "Page to start from",
          WhatTypeOfAccountPage,
          NewAccountDetailsPage
        )

        forAll(checkModeModeFTNAECoBTable) { (fromPage) =>
          fromOnePageToNextTest(
            fromPage,
            "confirmNewDetails",
            cobroutes.ConfirmNewAccountDetailsController.onPageLoad(CheckMode),
            newAccountDetailsUA,
            Some("(Check Mode)"),
            CheckMode
          )
        }
      }

      "FTNAE" - {
        val checkModeModeFTNAETable = Table(
          "Page to start from",
          WhichYoungPersonPage,
          WillYoungPersonBeStayingPage,
          SchoolOrCollegePage,
          TwelveHoursAWeekPage,
          HowManyYearsPage,
          LiveWithYouInUKPage,
          WillCourseBeEmployerProvidedPage
        )

        forAll(checkModeModeFTNAETable) { (fromPage) =>
          fromOnePageToNextTest(
            fromPage,
            "checkAnswersPage",
            ftnaeroutes.CheckYourAnswersController.onPageLoad(),
            happyPathFTNAEUA,
            Some("(Check Mode)"),
            CheckMode
          )
        }
      }
    }

    "Journey Recovery" - {
      val journeyRecoveryTable = Table(
        ("Page to start from", "return Call"),
        (WhichYoungPersonPage, ftnaeroutes.WhichYoungPersonController.onPageLoad(NormalMode)),
        (WillYoungPersonBeStayingPage, ftnaeroutes.WillYoungPersonBeStayingController.onPageLoad(NormalMode)),
        (SchoolOrCollegePage, ftnaeroutes.SchoolOrCollegeController.onPageLoad(NormalMode)),
        (TwelveHoursAWeekPage, ftnaeroutes.TwelveHoursAWeekController.onPageLoad(NormalMode)),
        (HowManyYearsPage, ftnaeroutes.HowManyYearsController.onPageLoad(NormalMode)),
        (WillCourseBeEmployerProvidedPage, ftnaeroutes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode)),
        (LiveWithYouInUKPage, ftnaeroutes.LiveWithYouInUKController.onPageLoad(NormalMode)),
        (ConfirmNewAccountDetailsPage, cobroutes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode))
      )

      forAll(journeyRecoveryTable) { (fromPage, returnCall) =>
        fromOnePageToNextTest(
          fromPage,
          "journeyRecovery",
          routes.JourneyRecoveryController.onPageLoad(Some(RedirectUrl(returnCall.url))),
          defaultUA,
          recovery,
          NormalMode
        )
      }
    }
  }
}
