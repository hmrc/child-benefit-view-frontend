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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlEqualTo}
import connectors.ChildBenefitEntitlementConnector
import handlers.ErrorHandler
import models.CBEnvelope
import models.common.{AddressLine, AddressPostcode}
import models.entitlement._
import models.errors.{CBError, ConnectorError}
import org.scalatest.EitherValues
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.AuthStub.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.NonceUtils.removeNonce
import utils.TestData.NinoUser
import views.html.ProofOfEntitlement

import java.time.LocalDate
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

class ProofOfEntitlementControllerSpec extends BaseISpec with EitherValues {

  val entitlementResult = ChildBenefitEntitlement(
    Claimant(
      FullName("John Doe"),
      500.00,
      LocalDate.now(),
      LocalDate.now.plusYears(3),
      1000.00,
      50.00,
      List(
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(2), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(3), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(4), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(5), 50.00),
        LastPaymentFinancialInfo(LocalDate.now().minusMonths(6), 50.00)
      ),
      FullAddress(
        AddressLine("AddressLine1"),
        AddressLine("AddressLine2"),
        Some(AddressLine("AddressLine3")),
        Some(AddressLine("AddressLine4")),
        Some(AddressLine("AddressLine5")),
        AddressPostcode("SS1 7JJ")
      )
    ),
    entitlementDate = LocalDate.now(),
    paidAmountForEldestOrOnlyChild = 25.10,
    paidAmountForEachAdditionalChild = 14.95,
    children = List(
      Child(
        FullName("Full Name"),
        dateOfBirth = LocalDate.of(2012, 1, 1),
        relationshipStartDate = LocalDate.of(2013, 1, 1),
        relationshipEndDate = Some(LocalDate.of(2016, 1, 1))
      )
    )
  )

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

        val errorHandler = application.injector.instanceOf[ErrorHandler]
        val connector    = application.injector.instanceOf[ChildBenefitEntitlementConnector]

        val maybeEntitlement = Await.result(connector.getChildBenefitEntitlement.value, 1.second)

        maybeEntitlement.isLeft mustBe true

        status(result) mustEqual INTERNAL_SERVER_ERROR
        assertSameHtmlAfter(removeNonce)(
          contentAsString(result),
          contentAsString(Future.successful(errorHandler.handleError(INTERNAL_SERVER_ERROR, "test-failure")))
        )
      }
    }

    "must return OK and render the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      stubFor(
        get(urlEqualTo("/child-benefit-service/view-entitlements-and-payments"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(Json.toJson(entitlementResult).toString)
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
          view(entitlementResult)(request, messages(application, request)).toString
        )
      }
    }
  }
}
