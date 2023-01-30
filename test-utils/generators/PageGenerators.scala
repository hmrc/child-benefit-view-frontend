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

package generators

import org.scalacheck.Arbitrary
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import pages.ftnae.{ExtendPaymentsPage, HowManyYearsPage, LiveWithYouInUKPage, SchoolOrCollegePage, TwelveHoursAWeekPage, WhichYoungPersonPage, WillCourseBeEmployerProvidedPage, WillYoungPersonBeStayingPage}

trait PageGenerators {

  implicit lazy val arbitrarySchoolOrCollegePage: Arbitrary[SchoolOrCollegePage.type] =
    Arbitrary(SchoolOrCollegePage)

  implicit lazy val arbitraryWillYoungPersonBeStayingPage: Arbitrary[WillYoungPersonBeStayingPage.type] =
    Arbitrary(WillYoungPersonBeStayingPage)

  implicit lazy val arbitraryWillCourseBeEmployerProvidedPage: Arbitrary[WillCourseBeEmployerProvidedPage.type] =
    Arbitrary(WillCourseBeEmployerProvidedPage)

  implicit lazy val arbitraryWhichYoungPersonPage: Arbitrary[WhichYoungPersonPage.type] =
    Arbitrary(WhichYoungPersonPage)

  implicit lazy val arbitraryTwelveHoursAWeekPage: Arbitrary[TwelveHoursAWeekPage.type] =
    Arbitrary(TwelveHoursAWeekPage)

  implicit lazy val arbitraryLiveWithYouInUKPage: Arbitrary[LiveWithYouInUKPage.type] =
    Arbitrary(LiveWithYouInUKPage)

  implicit lazy val arbitraryHowManyYearsPage: Arbitrary[HowManyYearsPage.type] =
    Arbitrary(HowManyYearsPage)

  implicit lazy val arbitraryExtendPaymentsPage: Arbitrary[ExtendPaymentsPage.type] =
    Arbitrary(ExtendPaymentsPage)

  implicit lazy val arbitraryConfirmNewAccountDetailsPage: Arbitrary[ConfirmNewAccountDetailsPage.type] =
    Arbitrary(ConfirmNewAccountDetailsPage)

  implicit lazy val arbitraryNewAccountDetailsPage: Arbitrary[NewAccountDetailsPage.type] =
    Arbitrary(NewAccountDetailsPage)
}
