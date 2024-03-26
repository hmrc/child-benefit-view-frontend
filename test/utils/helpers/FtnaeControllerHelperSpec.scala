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

package utils.helpers

import models.UserAnswers
import models.common.NationalInsuranceNumber
import models.ftnae.HowManyYears
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae._
import play.api.i18n.Messages
import play.api.libs.json.{JsBoolean, JsObject, JsString, JsValue}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.helpers.FtnaeControllerHelperSpec._
import utils.pages.QuestionPage
import viewmodels.checkAnswers.ftnae._

class FtnaeControllerHelperSpec extends AnyFreeSpec with MockitoSugar with Matchers with FtnaeControllerHelper {
  implicit val messages: Messages = mock[Messages]

  "buildSummaryRows" - {
    val fakeRequest = FakeRequest("GET", "unittest/buildSummaryRows")
    when(messages(anyString(), any())).thenAnswer(_.getArgument(0))

    "GIVEN all user answers rows supplied" - {
      "THEN all expected SummaryRows are returned" in {
        val expectedUserAnswersLength = 7
        val userAnswers = buildUserAnswers(
          answerField(WhichYoungPersonPage, testCRN),
          answerField(WillYoungPersonBeStayingPage, true),
          answerField(SchoolOrCollegePage, true),
          answerField(TwelveHoursAWeekPage, true),
          answerField(HowManyYearsPage, HowManyYears.Oneyear.toString),
          answerField(WillCourseBeEmployerProvidedPage, false),
          answerField(LiveWithYouInUKPage, true)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, userAnswers))

        val sutResult = buildSummaryRows(request)

        sutResult must not be None
        val rows = sutResult.get
        rows.length mustEqual expectedUserAnswersLength

        checkForRowSummaries(
          rows,
          WhichYoungPersonSummary.keyName,
          WillYoungPersonBeStayingSummary.keyName,
          SchoolOrCollegeSummary.keyName,
          TwelveHoursAWeekSummary.keyName,
          HowManyYearsSummary.keyName,
          WillCourseBeEmployerProvidedSummary.keyName,
          LiveWithYouInUKSummary.keyName
        )

        checkForRowSummariesAbsence(rows)
      }
    }

    "GIVEN a partial list of 'sequentially contiguous' user answers rows are supplied" - {
      "THEN only the respective SummaryRows are returned" in {
        val expectedUserAnswersLength = 2
        val userAnswers = buildUserAnswers(
          answerField(WhichYoungPersonPage, testCRN),
          answerField(WillYoungPersonBeStayingPage, true)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, userAnswers))

        val sutResult = buildSummaryRows(request)

        sutResult must not be None
        val rows = sutResult.get
        rows.length mustEqual expectedUserAnswersLength

        checkForRowSummaries(rows, WhichYoungPersonSummary.keyName, WillYoungPersonBeStayingSummary.keyName)

        checkForRowSummariesAbsence(
          rows,
          SchoolOrCollegeSummary.keyName,
          TwelveHoursAWeekSummary.keyName,
          HowManyYearsSummary.keyName,
          WillCourseBeEmployerProvidedSummary.keyName,
          LiveWithYouInUKSummary.keyName
        )
      }
    }

    "GIVEN a partial list of 'sequentially non-contiguous' user answers rows are supplied" - {
      "THEN only the respective SummaryRows are returned" in {
        val expectedUserAnswersLength = 4
        val userAnswers = buildUserAnswers(
          answerField(WhichYoungPersonPage, testCRN),
          answerField(WillYoungPersonBeStayingPage, true),
          answerField(WillCourseBeEmployerProvidedPage, false),
          answerField(LiveWithYouInUKPage, true)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, userAnswers))

        val sutResult = buildSummaryRows(request)

        sutResult must not be None
        val rows = sutResult.get
        rows.length mustEqual expectedUserAnswersLength

        checkForRowSummaries(
          rows,
          WhichYoungPersonSummary.keyName,
          WillYoungPersonBeStayingSummary.keyName,
          WillCourseBeEmployerProvidedSummary.keyName,
          LiveWithYouInUKSummary.keyName
        )

        checkForRowSummariesAbsence(
          rows,
          SchoolOrCollegeSummary.keyName,
          TwelveHoursAWeekSummary.keyName,
          HowManyYearsSummary.keyName
        )
      }
    }
  }
}

object FtnaeControllerHelperSpec {
  val testId   = "testId"
  val testNino = NationalInsuranceNumber("AA000000A")
  val testCRN  = "AA111111C"

  def buildUserAnswers(fields: (String, JsValue)*): JsObject =
    JsObject.apply(fields)

  def answerField(page: QuestionPage[_], value: String): (String, JsValue) =
    (page.toString, JsString(value))
  def answerField(page: QuestionPage[Boolean], value: Boolean): (String, JsValue) =
    (page.toString, JsBoolean(value))

  def checkForRowSummaries(rows: List[SummaryListRow], expectedKeys: String*) =
    expectedKeys.foreach(key => {
      val keyCount = rows.count(r => r.key.content.asInstanceOf[Text].value == key)
      assert(keyCount == 1, s"There should be exactly 1 SummaryListRow with the key $key: found $keyCount")
    })
  def checkForRowSummariesAbsence(rows: List[SummaryListRow], expectedKeys: String*) =
    expectedKeys.foreach(key => {
      val keyCount = rows.count(r => r.key.content.asInstanceOf[Text].value == key)
      assert(keyCount == 0, s"There should be no SummaryListRow with the key $key: found $keyCount")
    })
}
