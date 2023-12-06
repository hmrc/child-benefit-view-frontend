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

import models.cob.{ConfirmNewAccountDetails, NewAccountDetails}
import models.ftnae.HowManyYears
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages.cob._
import pages.ftnae._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitrarySchoolOrCollegeUserAnswersEntry: Arbitrary[(SchoolOrCollegePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SchoolOrCollegePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWillYoungPersonBeStayingUserAnswersEntry
      : Arbitrary[(WillYoungPersonBeStayingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WillYoungPersonBeStayingPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWillCourseBeEmployerProvidedUserAnswersEntry
      : Arbitrary[(WillCourseBeEmployerProvidedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WillCourseBeEmployerProvidedPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhichYoungPersonUserAnswersEntry: Arbitrary[(WhichYoungPersonPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhichYoungPersonPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTwelveHoursAWeekUserAnswersEntry: Arbitrary[(TwelveHoursAWeekPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TwelveHoursAWeekPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryLiveWithYouInUKUserAnswersEntry: Arbitrary[(LiveWithYouInUKPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[LiveWithYouInUKPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowManyYearsUserAnswersEntry: Arbitrary[(HowManyYearsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowManyYearsPage.type]
        value <- arbitrary[HowManyYears].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryExtendPaymentsUserAnswersEntry: Arbitrary[(ExtendPaymentsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ExtendPaymentsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConfirmNewAccountDetailsUserAnswersEntry
      : Arbitrary[(ConfirmNewAccountDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConfirmNewAccountDetailsPage.type]
        value <- arbitrary[ConfirmNewAccountDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNewAccountDetailsUserAnswersEntry: Arbitrary[(NewAccountDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NewAccountDetailsPage.type]
        value <- arbitrary[NewAccountDetails].map(Json.toJson(_))
      } yield (page, value)
    }
}
