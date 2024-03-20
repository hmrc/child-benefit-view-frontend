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

package services

import connectors.FtnaeConnector
import models.common.{ChildReferenceNumber, FirstForename, NationalInsuranceNumber, Surname}
import models.errors.FtnaeChildUserAnswersNotRetrieved
import models.ftnae.{ChildDetails, CourseDuration, FtnaeChildInfo, FtnaeClaimantInfo, FtnaeQuestionAndAnswer, FtnaeResponse, HowManyYears}
import models.requests.{DataRequest, FtnaePaymentsExtendedPageDataRequest}
import models.{CBEnvelope, UserAnswers}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ftnae.{FtnaeResponseUserAnswer, HowManyYearsPage, WhichYoungPersonPage}
import play.api.i18n.{DefaultMessagesApi, Messages}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.{FtnaePaymentsExtendedPageSessionRepository, SessionRepository}
import services.FtnaeServiceSpec._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class FtnaeServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures with ScalaCheckPropertyChecks {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()
  val testMessages: Map[String, Map[String, String]] = Map(
    "default" -> Map("title" -> "foo bar")
  )
  val messagesApi = new DefaultMessagesApi(testMessages)
  implicit val messages: Messages = messagesApi.preferred(FakeRequest("GET", "/"))

  val ftnaeConnector: FtnaeConnector = mock[FtnaeConnector]
  val sessionRepository: SessionRepository = mock[SessionRepository]
  val ftnaePaymentsExtendedPageSessionRepository: FtnaePaymentsExtendedPageSessionRepository = mock[FtnaePaymentsExtendedPageSessionRepository]

  val sut: FtnaeService =
    new FtnaeService(ftnaeConnector, sessionRepository, ftnaePaymentsExtendedPageSessionRepository)

  val childName = "Lauren Sam Smith"
  val childDetails: ChildDetails = ChildDetails(
    CourseDuration.TwoYear,
    ChildReferenceNumber("AC654321C"),
    LocalDate.of(2007, 2, 10),
    "Lauren Sam Smith",
    testAuditAnswers
  )

  "submitFtnaeInformation" should {
    "submit a user account information if the data recorded in user answers are valid, " +
      "one child has the same name that the user selected, " +
      "all crns are unique, " +
      "course duration is valid" +
      "and users answers supplied" in {
      reset(ftnaeConnector)
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val fakeRequest = FakeRequest("GET", "/nothing")
      val userAnswers = buildUserAnswers(
        ftnaeResponseField(createFtnaeResponseWithChildren()),
        whichYoungPersonField(testName),
        howManyYearsField()
      )

      val request: DataRequest[AnyContent] =
        DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

      whenReady(sut.submitFtnaeInformation(Some(testSummaryListRows))(ec, hc, request, messages).value) { result =>
        verify(ftnaeConnector, times(1))
          .uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())

        result mustBe Right((testName, toChildDetails(childInfoA)))
      }
    }

    "should NOT call uploadFtnaeDetails if it is a FtnaePaymentsExtended session" in {
      reset(ftnaeConnector)
      val fakeRequest = FakeRequest("GET", "/nothing")
      val userAnswers = buildUserAnswers(
        ftnaeResponseField(createFtnaeResponseWithChildren()),
        whichYoungPersonField(testName),
        howManyYearsField()
      )

      val request: FtnaePaymentsExtendedPageDataRequest[AnyContent] =
        FtnaePaymentsExtendedPageDataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

      whenReady(sut.submitFtnaeInformation(Some(testSummaryListRows))(ec, hc, request, messages).value) { result =>
        verify(ftnaeConnector, times(0))
          .uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())

        result mustBe Right((testName, toChildDetails(childInfoA)))
      }
    }

    "return an error if course duration is not set as One year or Two Year" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val userAnswers = buildUserAnswers(
        ftnaeResponseField(createFtnaeResponseWithChildren()),
        whichYoungPersonField("Invalid Value"),
        howManyYearsField()
      )
      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          testId,
          testNino,
          UserAnswers(testId, data = userAnswers)
        )

      whenReady(sut.submitFtnaeInformation(None)(ec, hc, request, messages).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }

    "return an error if there are multiple children with the same crn" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val userAnswers = buildUserAnswers(
        ftnaeResponseField(createFtnaeResponseWithChildren(List(childInfoA, childInfoB.copy(crn = childInfoA.crn)))),
        whichYoungPersonField(testName),
        howManyYearsField()
      )
      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          "id",
          testNino,
          UserAnswers("id", data = userAnswers)
        )

      whenReady(sut.submitFtnaeInformation(None)(ec, hc, request, messages).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }

    "return error if there are multiple children with the same name" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val userAnswers = buildUserAnswers(
        ftnaeResponseField(
          createFtnaeResponseWithChildren(
            List(childInfoA, childInfoB.copy(name = childInfoA.name, lastName = childInfoA.lastName))
          )
        ),
        whichYoungPersonField(testName),
        howManyYearsField()
      )
      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          "id",
          testNino,
          UserAnswers("id", data = userAnswers)
        )

      whenReady(sut.submitFtnaeInformation(None)(ec, hc, request, messages).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }

    "submit a user account information if the data" +
      "recorded in user answers are valid and confirm that cleaning database is performed" in {
      reset(sessionRepository)

      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      when(sessionRepository.clear(anyString())).thenReturn(Future.successful(true))

      val fakeRequest = FakeRequest("GET", "/nothing")
      val userAnswers = buildUserAnswers(
        ftnaeResponseField(createFtnaeResponseWithChildren()),
        whichYoungPersonField(testName),
        howManyYearsField()
      )

      val request: DataRequest[AnyContent] =
        DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

      whenReady(sut.submitFtnaeInformation(Some(testSummaryListRows))(ec, hc, request, messages).value) { result =>
        result mustBe Right((testName, toChildDetails(childInfoA)))
      }

      verify(sessionRepository, times(1)).clear(anyString())

    }

    "submitFtnaeAuditInformation" should {
      "return list of audit items from user selected questions and answers" in {
        val auditData = sut.buildAuditData(Some(testSummaryListRows))
        auditData mustBe testAuditAnswers
      }
    }
  }

  "getSelectedChildInfo" should {
    val fakeRequest = FakeRequest("GET", "/unittest/getSelectedChildInfo")
    "GIVEN a valid list of Children AND a matching CRN" should {
      "THEN the expected child should be returned" in {
        val children  = List(childInfoA, childInfoB, childInfoC)
        val childName = s"${childInfoA.name.value} ${childInfoA.lastName.value}"
        val userAnswers = buildUserAnswers(
          ftnaeResponseField(createFtnaeResponseWithChildren(children)),
          whichYoungPersonField(childName)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

        val selectedChild = sut.getSelectedChildInfo(request)

        selectedChild mustEqual Some(childInfoA)
      }
    }

    "GIVEN a valid list of Children AND no matching CRN" should {
      "THEN None should be returned" in {
        val children  = List(childInfoA, childInfoB, childInfoC)
        val childName = "Not in the list"
        val userAnswers = buildUserAnswers(
          ftnaeResponseField(createFtnaeResponseWithChildren(children)),
          whichYoungPersonField(childName)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

        val selectedChild = sut.getSelectedChildInfo(request)

        selectedChild mustEqual None
      }
    }

    "GIVEN a invalid list with duplicate CRNs" should {
      "THEN None should be returned" in {
        val duplicatedCRNChild = childInfoA.copy(crn = childInfoB.crn)
        val children           = List(duplicatedCRNChild, childInfoB, childInfoC)
        val childName          = "Should not be relevant"
        val userAnswers = buildUserAnswers(
          ftnaeResponseField(createFtnaeResponseWithChildren(children)),
          whichYoungPersonField(childName)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

        val selectedChild = sut.getSelectedChildInfo(request)

        selectedChild mustEqual None
      }
    }

    "GIVEN a invalid list with duplicate names" should {
      "THEN None should be returned" in {
        val duplicatedNameChild = childInfoA.copy(name = childInfoB.name)
        val children            = List(duplicatedNameChild, childInfoB, childInfoC)
        val childName           = "Should not be relevant"
        val userAnswers = buildUserAnswers(
          ftnaeResponseField(createFtnaeResponseWithChildren(children)),
          whichYoungPersonField(childName)
        )

        val request: DataRequest[AnyContent] =
          DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, data = userAnswers))

        val selectedChild = sut.getSelectedChildInfo(request)

        selectedChild mustEqual None
      }
    }
  }

  "getSelectedCourseDuration" should {
    val fakeRequest = FakeRequest("GET", "/unittest/getSelectedCourseDuration")
    val testCases = Table(
      ("Name", "HowManyYears", "ExpectedCourseDuration"),
      ("One Year", HowManyYears.Oneyear, Some(CourseDuration.OneYear)),
      ("Two Years", HowManyYears.Twoyears, Some(CourseDuration.TwoYear)),
      ("Other", HowManyYears.Other, None)
    )

    forAll(testCases) { (name, howManyYears, expectedCourseDuration) =>
      {
        s"Test Case: ${name}" should {
          s"GIVEN the HowManyYears case ${howManyYears.toString}" should {
            s"THEN should return the Option[CourseDuration] case ${expectedCourseDuration
              .fold("None")(c => s"Some(${c.toString})")}" in {
              val userAnswers = buildUserAnswers(
                howManyYearsField(howManyYears)
              )

              val request: DataRequest[AnyContent] =
                DataRequest(fakeRequest, testId, testNino, UserAnswers(testId, userAnswers))

              val courseDuration = sut.getSelectedCourseDuration(request)

              courseDuration mustEqual expectedCourseDuration
            }
          }
        }
      }
    }
  }
}

object FtnaeServiceSpec {
  val testNino = NationalInsuranceNumber("nino")
  val testId   = "unitTestId"
  val testCRN  = "AA111111A"
  val testName = "A Child"

  val childInfoA: FtnaeChildInfo = FtnaeChildInfo(
    ChildReferenceNumber("AA111111A"),
    FirstForename("A"),
    None,
    Surname("Child"),
    LocalDate.now().minusYears(3),
    LocalDate.now().plusYears(13)
  )

  val childInfoB: FtnaeChildInfo = FtnaeChildInfo(
    ChildReferenceNumber("BB222222B"),
    FirstForename("B"),
    None,
    Surname("Child"),
    LocalDate.now().minusYears(7),
    LocalDate.now().plusYears(9)
  )

  val childInfoC: FtnaeChildInfo = FtnaeChildInfo(
    ChildReferenceNumber("CC333333C"),
    FirstForename("C"),
    None,
    Surname("Child"),
    LocalDate.now().minusYears(12),
    LocalDate.now().plusYears(4)
  )

  val testSummaryListRows = List(
    SummaryListRow(Key(HtmlContent("user-question-1")), Value(HtmlContent("user-answer-1")))
  )
  val testAuditAnswers = List(FtnaeQuestionAndAnswer("user-question-1", "user-answer-1"))

  def toChildDetails(
      childInfo:           FtnaeChildInfo,
      courseDuration:      CourseDuration = CourseDuration.OneYear,
      questionsAndAnswers: List[FtnaeQuestionAndAnswer] = testAuditAnswers
  ): ChildDetails =
    ChildDetails(
      courseDuration,
      childInfo.crn,
      childInfo.dateOfBirth,
      s"${childInfo.name.value} ${childInfo.lastName.value}",
      questionsAndAnswers
    )

  def createFtnaeResponseWithChildren(
      children: List[FtnaeChildInfo] = List(childInfoA, childInfoB, childInfoC)
  ): FtnaeResponse =
    FtnaeResponse(
      FtnaeClaimantInfo(FirstForename("Jayne"), Surname("Doe")),
      children
    )

  def buildUserAnswers(fields: (String, JsValue)*): JsObject = JsObject.apply(fields)

  def ftnaeResponseField(response: FtnaeResponse): (String, JsValue) =
    (FtnaeResponseUserAnswer.toString, Json.toJson(response))
  def whichYoungPersonField(crn: String): (String, JsValue) =
    (WhichYoungPersonPage.toString, JsString(crn))
  def howManyYearsField(howManyYears: HowManyYears = HowManyYears.Oneyear) =
    (HowManyYearsPage.toString, JsString(howManyYears.toString))

  def courseDurationToHowManyYears(courseDuration: CourseDuration): String =
    courseDuration match {
      case CourseDuration.OneYear => "oneyear"
      case CourseDuration.TwoYear => "twoyear"
    }
}
