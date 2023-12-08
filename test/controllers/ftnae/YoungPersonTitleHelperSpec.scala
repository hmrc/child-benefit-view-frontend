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

package controllers.ftnae

import base.BaseAppSpec
import models.common._
import models.ftnae.{FtnaeChildInfo, FtnaeClaimantInfo, FtnaeResponse}
import models.requests.DataRequest
import models.{CheckMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.ftnae.{FtnaeResponseUserAnswer, WhichYoungPersonPage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.helpers
import utils.helpers.YoungPersonTitleHelper

class YoungPersonTitleHelperSpec extends BaseAppSpec with MockitoSugar with FtnaeFixture {

  val defaultFtnaeResponse = FtnaeResponse(
    FtnaeClaimantInfo(FirstForename("fn"), Surname("sa")),
    List(
      FtnaeChildInfo(
        ChildReferenceNumber("crn1234"),
        FirstForename("XXX"),
        None,
        Surname("YYY"),
        sixteenBy1stOfSeptemberThisYear,
        getFirstMondayOfSeptemberThisYear()
      )
    )
  )

  "YoungPersonTitleHelper Utility" - {
    "must retrieve fist name if match is found" in {

      val childList = childInfoList("First-Name", "surname")

      val ftnaeResponse = defaultFtnaeResponse.copy(children = childList)

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First-Name Surname")
        .flatMap(_.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      val fakeRequest = FakeRequest(GET, controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(CheckMode).url)
        .withFormUrlEncodedBody(("value", "*** name here doesn't matter ***"))

      val request: DataRequest[AnyContent] = DataRequest(fakeRequest, "", NationalInsuranceNumber("XXX"), userAnswers)

      YoungPersonTitleHelper(request)
        .firstNameFromConcatenatedChildNames() mustBe Some("First-Name")
    }

    "must retrieve None if match is not found" in {

      val childList = childInfoList("XXXFirst-Name", "surname")

      val ftnaeResponse = defaultFtnaeResponse.copy(children = childList)

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First-Name Surname")
        .flatMap(_.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      val fakeRequest = FakeRequest(GET, controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(CheckMode).url)
        .withFormUrlEncodedBody(("value", "*** name here doesn't matter ***"))

      val request: DataRequest[AnyContent] = DataRequest(fakeRequest, "", NationalInsuranceNumber("XXX"), userAnswers)

      helpers
        .YoungPersonTitleHelper(request)
        .firstNameFromConcatenatedChildNames() mustBe None
    }

    "must retrieve None if child info list is empty" in {

      val ftnaeResponse = defaultFtnaeResponse.copy(children = List.empty)

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhichYoungPersonPage, "First-Name Surname")
        .flatMap(_.set(FtnaeResponseUserAnswer, ftnaeResponse))
        .success
        .value

      val fakeRequest = FakeRequest(GET, controllers.ftnae.routes.WhichYoungPersonController.onPageLoad(CheckMode).url)
        .withFormUrlEncodedBody(("value", "*** name here doesn't matter ***"))

      val request: DataRequest[AnyContent] = DataRequest(fakeRequest, "", NationalInsuranceNumber("XXX"), userAnswers)

      helpers
        .YoungPersonTitleHelper(request)
        .firstNameFromConcatenatedChildNames() mustBe None
    }

  }

  private def childInfoList(firstName: String, surname: String) = {
    List(
      defaultFtnaeResponse.children.head
        .copy(name = FirstForename(firstName), lastName = Surname(surname))
    )
  }
}
