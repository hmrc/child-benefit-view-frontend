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

package controllers.ftnae

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{NormalMode, UserAnswers}
import models.ftnae.{HowManyYears, WhichYoungPerson}
import models.viewmodels.checkAnswers.{HowManyYearsSummary, LiveWithYouInUKSummary, SchoolOrCollegeSummary, TwelveHoursAWeekSummary, WhichYoungPersonSummary, WillCourseBeEmployerProvidedSummary, WillYoungPersonBeStayingSummary}
import pages.ftnae.{HowManyYearsPage, LiveWithYouInUKPage, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage, WillYoungPersonBeStayingPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import models.viewmodels.govuk.summarylist._
import views.html.ftnae.CheckYourAnswersView

import scala.concurrent.ExecutionContext
import cats.syntax.either._
import controllers.ChildBenefitBaseController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, FeatureFlagComposedActions}

final case class UnansweredPageName(pageName: String) extends AnyVal
final case class KickedOutPageName(pageName: String) extends AnyVal
final case class PageDirections(pageUrlCall: Call, kickoutCall: Call)

object ThreePin {
  type ThreePin = Either[Either[UnansweredPageName, KickedOutPageName], Unit]
  def unansweredPageName(pageName: String): ThreePin =
    UnansweredPageName(pageName).asLeft[KickedOutPageName].asLeft[Unit]
  def kickedOutPageName(pageName: String): ThreePin =
    KickedOutPageName(pageName).asRight[UnansweredPageName].asLeft[Unit]
  def successUrl(): ThreePin = ().asRight[Either[UnansweredPageName, KickedOutPageName]]
}

class CheckYourAnswersController @Inject() (
    override val messagesApi: MessagesApi,
    authConnector:            AuthConnector,
    featureActions:           FeatureFlagComposedActions,
    getData:                  DataRetrievalAction,
    requireData:              DataRequiredAction,
    view:                     CheckYourAnswersView
)(implicit
    config:            Configuration,
    env:               Environment,
    ec:                ExecutionContext,
    cc:                MessagesControllerComponents,
    frontendAppConfig: FrontendAppConfig
) extends ChildBenefitBaseController(authConnector)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = {
    implicit val loginContinueUrl: Call = controllers.ftnae.routes.CheckYourAnswersController.onPageLoad
    (featureActions.ftnaeAction andThen authorisedAsChildBenefitUser andThen getData andThen requireData) {
      implicit request =>
        {
          val summaryRows = for {
            whichYoungPersonRow             <- WhichYoungPersonSummary.row(request.userAnswers)
            willYoungPersonBeStayingRow     <- WillYoungPersonBeStayingSummary.row(request.userAnswers)
            schoolOrCollegeRow              <- SchoolOrCollegeSummary.row(request.userAnswers)
            twelveHoursAWeekRow             <- TwelveHoursAWeekSummary.row(request.userAnswers)
            howManyYearsRow                 <- HowManyYearsSummary.row(request.userAnswers)
            willCourseBeEmployerProvidedRow <- WillCourseBeEmployerProvidedSummary.row(request.userAnswers)
            liveWithYouInUKRow              <- LiveWithYouInUKSummary.row(request.userAnswers)
          } yield List(
            whichYoungPersonRow,
            willYoungPersonBeStayingRow,
            schoolOrCollegeRow,
            twelveHoursAWeekRow,
            howManyYearsRow,
            willCourseBeEmployerProvidedRow,
            liveWithYouInUKRow
          )

          firstKickedOutOrUnansweredOtherwiseSuccess(request.userAnswers) match {
            case Right(()) =>
              summaryRows.fold(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))(sr =>
                Ok(view(SummaryListViewModel(sr)))
              )
            case Left(Left(unansweredUrl)) =>
              Redirect(pageDirections(unansweredUrl.pageName).pageUrlCall)
            case Left(Right(kickedOutUrl)) =>
              Redirect(pageDirections(kickedOutUrl.pageName).kickoutCall)
          }
        }
    }
  }

  private def pageDirections: Map[String, PageDirections] =
    Map(
      "whichYoungPerson" ->
        PageDirections(
          controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(NormalMode),
          controllers.ftnae.routes.WhyYoungPersonNotListedController.onPageLoad()
        ),
      "willYoungPersonBeStaying" ->
        PageDirections(
          controllers.ftnae.routes.WillYoungPersonBeStayingController.onPageLoad(NormalMode),
          controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
        ),
      "schoolOrCollege" -> PageDirections(
        controllers.ftnae.routes.SchoolOrCollegeController.onPageLoad(NormalMode),
        controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      ),
      "twelveHoursAWeek" ->
        PageDirections(
          controllers.ftnae.routes.TwelveHoursAWeekController.onPageLoad(NormalMode),
          controllers.ftnae.routes.NotEntitledController.onPageLoad()
        ),
      "howManyYears" -> PageDirections(
        controllers.ftnae.routes.HowManyYearsController.onPageLoad(NormalMode),
        controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      ),
      "willCourseBeEmployerProvided" ->
        PageDirections(
          controllers.ftnae.routes.WillCourseBeEmployerProvidedController.onPageLoad(NormalMode),
          controllers.ftnae.routes.NotEntitledCourseEmployerProvidedController.onPageLoad()
        ),
      "liveWithYouInUK" -> PageDirections(
        controllers.ftnae.routes.LiveWithYouInUKController.onPageLoad(NormalMode),
        controllers.ftnae.routes.UseDifferentFormController.onPageLoad()
      )
    )

  private def firstKickedOutOrUnansweredOtherwiseSuccess(
      userAnswers: UserAnswers
  ): ThreePin.ThreePin =
    for {
      _ <-
        userAnswers
          .get(WhichYoungPersonPage)
          .fold(ThreePin.unansweredPageName(WhichYoungPersonPage.toString))(answer =>
            if (answer == WhichYoungPerson.ChildNotListed) { ThreePin.kickedOutPageName(WhichYoungPersonPage.toString) }
            else { ThreePin.successUrl() }
          )
      _ <-
        userAnswers
          .get(WillYoungPersonBeStayingPage)
          .fold(ThreePin.unansweredPageName(WillYoungPersonBeStayingPage.toString))(answer =>
            if (answer) ThreePin.successUrl() else ThreePin.kickedOutPageName(WillYoungPersonBeStayingPage.toString)
          )
      _ <-
        userAnswers
          .get(SchoolOrCollegePage)
          .fold(ThreePin.unansweredPageName(SchoolOrCollegePage.toString))(answer =>
            if (answer) ThreePin.successUrl() else ThreePin.kickedOutPageName(SchoolOrCollegePage.toString)
          )
      _ <-
        userAnswers
          .get(TwelveHoursAWeekPage)
          .fold(ThreePin.unansweredPageName(TwelveHoursAWeekPage.toString))(answer =>
            if (answer) ThreePin.successUrl() else ThreePin.kickedOutPageName(TwelveHoursAWeekPage.toString)
          )
      _ <-
        userAnswers
          .get(HowManyYearsPage)
          .fold(ThreePin.unansweredPageName(HowManyYearsPage.toString))(answer =>
            if (answer == HowManyYears.Other) ThreePin.kickedOutPageName(HowManyYearsPage.toString)
            else ThreePin.successUrl()
          )
      _ <-
        userAnswers
          .get(WillCourseBeEmployerProvidedPage)
          .fold(ThreePin.unansweredPageName(WillCourseBeEmployerProvidedPage.toString))(answer =>
            if (!answer) { ThreePin.successUrl() }
            else { ThreePin.kickedOutPageName(WillCourseBeEmployerProvidedPage.toString) }
          )
      _ <-
        userAnswers
          .get(LiveWithYouInUKPage)
          .fold(ThreePin.unansweredPageName(LiveWithYouInUKPage.toString))(answer =>
            if (answer) ThreePin.successUrl() else ThreePin.kickedOutPageName(LiveWithYouInUKPage.toString)
          )
    } yield ()

}
