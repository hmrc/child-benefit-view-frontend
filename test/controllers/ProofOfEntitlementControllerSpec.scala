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

package controllers

import cats.data.EitherT
import connectors.ChildBenefitEntitlementConnector
import models.CBEnvelope
import models.entitlement.ChildBenefitEntitlement
import models.errors.{CBError, ConnectorError}
import org.scalatest.EitherValues
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.AuthStub.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.NonceUtils.removeNonce
import utils.TestData.NinoUser
import views.html.{ErrorTemplate, ProofOfEntitlement}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class ProofOfEntitlementControllerSpec extends BaseISpec with EitherValues {
  "Proof of entitlement controller" - {
    "must return INTERNAL_SERVER_ERROR and render the error view for a GET when getting entitlement fails" in {
      userLoggedInChildBenefitUser(NinoUser)

      val failingChildBenefitEntitlementConnector = new ChildBenefitEntitlementConnector {
        override def getChildBenefitEntitlement(implicit
            ec: ExecutionContext,
            hc: HeaderCarrier
        ): CBEnvelope[ChildBenefitEntitlement] =
          EitherT[Future, CBError, ChildBenefitEntitlement](
            Future.successful(
              Left(ConnectorError(INTERNAL_SERVER_ERROR, "test-failure"))
            )
          )
      }

      val application = applicationBuilder(entitlementConnector =
        bind[ChildBenefitEntitlementConnector].toInstance(failingChildBenefitEntitlementConnector)
      ).build()

      running(application) {
        implicit val ec: ExecutionContextExecutor = application.actorSystem.getDispatcher

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.ProofOfEntitlementController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view        = application.injector.instanceOf[ErrorTemplate]
        val connector   = application.injector.instanceOf[ChildBenefitEntitlementConnector]
        val messagesApi = messages(application, request)

        val maybeEntitlement = Await.result(connector.getChildBenefitEntitlement.value, 1.second)

        maybeEntitlement.isLeft mustBe true

        status(result) mustEqual INTERNAL_SERVER_ERROR
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(
            messagesApi("global.error.InternalServerError500.title"),
            messagesApi("global.error.InternalServerError500.heading"),
            "test-failure"
          )(request, messagesApi).toString
        )
      }
    }

    "must return OK and render the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder().build()

      running(application) {
        implicit val ec: ExecutionContextExecutor = application.actorSystem.getDispatcher

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.ProofOfEntitlementController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view      = application.injector.instanceOf[ProofOfEntitlement]
        val connector = application.injector.instanceOf[ChildBenefitEntitlementConnector]

        val maybeEntitlement = Await.result(connector.getChildBenefitEntitlement.value, 1.second)
        val entitlement      = maybeEntitlement.value

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(entitlement)(request, messages(application, request)).toString
        )
      }
    }
  }
}
