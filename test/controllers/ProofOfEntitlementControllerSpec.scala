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

package controllers

import base.BaseAppSpec
import cats.data.EitherT
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import connectors.ChildBenefitEntitlementConnector
import models.CBEnvelope.CBEnvelope
import models.entitlement._
import models.errors.{CBError, ConnectorError}
import models.pertaxAuth.PertaxAuthResponseModel
import org.scalatest.EitherValues
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.AuditService
import stubs.AuthStubs._
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeNonce
import utils.TestData.{ninoUser, testEntitlement}
import utils.handlers.ErrorHandler
import views.html.ProofOfEntitlement

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class ProofOfEntitlementControllerSpec extends BaseAppSpec with EitherValues {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/")
  implicit val auditor: AuditService                        = mock[AuditService]
  implicit val ec:      ExecutionContext                    = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc:      HeaderCarrier                       = HeaderCarrier()

  "Proof of entitlement controller" - {
    "must return SEE_OTHER and redirect to the service down view for a GET when getting entitlement fails" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))

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

      val application = applicationBuilder()
        .overrides(
          bind[ChildBenefitEntitlementConnector].toInstance(failingChildBenefitEntitlementConnector)
        )
        .build()

      running(application) {
        implicit val ec: ExecutionContextExecutor = application.actorSystem.getDispatcher

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.ProofOfEntitlementController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val errorHandler = application.injector.instanceOf[ErrorHandler]
        val connector    = application.injector.instanceOf[ChildBenefitEntitlementConnector]

        val maybeEntitlement = Await.result(connector.getChildBenefitEntitlement.value, 1.second)

        maybeEntitlement.isLeft mustBe true

        status(result) mustEqual SEE_OTHER
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          contentAsString(
            Future.successful(errorHandler.handleError(ConnectorError(INTERNAL_SERVER_ERROR, "test-failure")))
          )
        )
      }
    }

    "must return OK and render the correct view for a GET" in {
      userLoggedInIsChildBenefitUser(ninoUser)
      mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))

      stubFor(
        get(urlEqualTo("/child-benefit-service/view-entitlements-and-payments"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(testEntitlement).toString)
          )
      )

      val application =
        applicationBuilder(Map("microservice.services.child-benefit-entitlement.port" -> wiremockPort)).build()

      running(application) {

        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.ProofOfEntitlementController.view.url).withSession("authToken" -> "Bearer 123")

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProofOfEntitlement]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          view(testEntitlement)(request, messages(application, request)).toString
        )
      }
    }
  }
}
