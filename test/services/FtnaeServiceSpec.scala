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

import connectors.FtneaConnector
import models.common.ChildReferenceNumber
import models.errors.FtnaeChildUserAnswersNotRetrieved
import models.ftnae.{ChildDetails, CourseDuration}
import models.requests.DataRequest
import models.{CBEnvelope, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import services.FtnaeServiceSpec.{userAnswer, userAnswerWithIncorrectCourseDuration, userAnswerWithMultipleSameCRN, userAnswerWithMultipleSameName}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class FtnaeServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier    = HeaderCarrier()

  val ftnaeConnector = mock[FtneaConnector]

  val childName    = "Lauren Sam Smith"
  val childDetails = ChildDetails(CourseDuration.TwoYear, ChildReferenceNumber("AC654321C"), LocalDate.of(2007, 2, 10))

  "submitFtnaeInformation" should {
    "submit a user account information if the data recorded in user answers are valid, " +
      "one child has the same name that the user selected, " +
      "all crns are unique, " +
      "course duration is valid" in {

      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val request: DataRequest[AnyContent] =
        DataRequest(FakeRequest("GET", "/nothing"), "id", UserAnswers("id", data = userAnswer))

      val service = new FtnaeService(ftnaeConnector)

      whenReady(service.submitFtnaeInformation()(ec, hc, request).value) { result =>
        result mustBe Right((childName, childDetails))
      }
    }

    "return an error if course duration is not set as One year or Two Year" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          "id",
          UserAnswers("id", data = userAnswerWithIncorrectCourseDuration)
        )

      val service = new FtnaeService(ftnaeConnector)

      whenReady(service.submitFtnaeInformation()(ec, hc, request).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }

    "return an error if there are multiple children with the same crn" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          "id",
          UserAnswers("id", data = userAnswerWithMultipleSameCRN)
        )

      val service = new FtnaeService(ftnaeConnector)

      whenReady(service.submitFtnaeInformation()(ec, hc, request).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }

    "return error if there are multiple children with the same name" in {
      when(
        ftnaeConnector.uploadFtnaeDetails(any[ChildDetails]())(any[ExecutionContext](), any[HeaderCarrier]())
      ) thenReturn CBEnvelope(())

      val request: DataRequest[AnyContent] =
        DataRequest(
          FakeRequest("GET", "/nothing"),
          "id",
          UserAnswers("id", data = userAnswerWithMultipleSameName)
        )

      val service = new FtnaeService(ftnaeConnector)

      whenReady(service.submitFtnaeInformation()(ec, hc, request).value) { result =>
        result mustBe Left(FtnaeChildUserAnswersNotRetrieved)
      }
    }
  }
}

object FtnaeServiceSpec {

  private val ftnaeResponse: JsValue = Json.parse(
    """
      |{
      |	"claimant": {
      |		"name": "Luke",
      |		"surname": "Smith"
      |	},
      |	"children": [{
      |		"crn": "AC654321C",
      |		"name": "Lauren",
      |		"midName": "Sam",
      |		"lastName": "Smith",
      |		"dateOfBirth": "2007-02-10",
      |		"currentClaimEndDate": "2023-09-04"
      |	}]
      |}
      |""".stripMargin
  )

  private val ftnaeResponseWithMultipleCRN: JsValue = Json.parse(
    """
      |{
      |	"claimant": {
      |		"name": "Luke",
      |		"surname": "Smith"
      |	},
      |	"children": [{
      |		"crn": "AC654321C",
      |		"name": "Lauren",
      |		"midName": "Sam",
      |		"lastName": "Smith",
      |		"dateOfBirth": "2007-02-10",
      |		"currentClaimEndDate": "2023-09-04"
      |	},{
      |		"crn": "AC654321C",
      |		"name": "James",
      |		"midName": "Sam",
      |		"lastName": "Smith",
      |		"dateOfBirth": "2007-02-10",
      |		"currentClaimEndDate": "2023-09-04"
      |	}
      | ]
      |}
      |""".stripMargin
  )

  private val ftnaeResponseWithMultipleSameName: JsValue = Json.parse(
    """
      |{
      |	"claimant": {
      |		"name": "Luke",
      |		"surname": "Smith"
      |	},
      |	"children": [{
      |		"crn": "AC654321C",
      |		"name": "Lauren",
      |		"midName": "Sam",
      |		"lastName": "Smith",
      |		"dateOfBirth": "2007-02-10",
      |		"currentClaimEndDate": "2023-09-04"
      |	},{
      |		"crn": "AC111111C",
      |		"name": "Lauren",
      |		"midName": "Sam",
      |		"lastName": "Smith",
      |		"dateOfBirth": "2007-01-10",
      |		"currentClaimEndDate": "2023-09-04"
      |	}
      | ]
      |}
      |""".stripMargin
  )

  val userAnswer: JsObject = JsObject.apply(
    Seq(
      (
        "ftneaResponseUserAnswer",
        ftnaeResponse
      ),
      ("whichYoungPerson", JsString("Lauren Sam Smith")),
      ("howManyYears", JsString("twoyears"))
    )
  )

  val userAnswerWithIncorrectCourseDuration: JsObject = JsObject.apply(
    Seq(
      (
        "ftneaResponseUserAnswer",
        ftnaeResponse
      ),
      ("whichYoungPerson", JsString("Lauren Sam Smith")),
      ("howManyYears", JsString("other"))
    )
  )

  val userAnswerWithMultipleSameName: JsObject = JsObject.apply(
    Seq(
      (
        "ftneaResponseUserAnswer",
        ftnaeResponseWithMultipleSameName
      ),
      ("whichYoungPerson", JsString("Lauren Sam Smith")),
      ("howManyYears", JsString("other"))
    )
  )

  val userAnswerWithMultipleSameCRN: JsObject = JsObject.apply(
    Seq(
      (
        "ftneaResponseUserAnswer",
        ftnaeResponseWithMultipleCRN
      ),
      ("whichYoungPerson", JsString("Lauren Sam Smith")),
      ("howManyYears", JsString("other"))
    )
  )
}
