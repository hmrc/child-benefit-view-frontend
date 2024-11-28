/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import base.BaseAppSpec
import config.FrontendAppConfig
import models.common.{AddressLine, AddressPostcode}
import models.entitlement.{Child, ChildBenefitEntitlement, Claimant, FullAddress, FullName, LastPaymentFinancialInfo}
import models.errors.{CBError, ConnectorError}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNAUTHORIZED}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, UpstreamErrorResponse}
import utils.TestData.{genericCBError, invalidJsonResponse, validNotMatchingJsonResponse}

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ChildBenefitEntitlementConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier:    HeaderCarrier    = new HeaderCarrier()

  override implicit lazy val app: Application                      = applicationBuilder().build()
  val sutWithStubs:               ChildBenefitEntitlementConnector = app.injector.instanceOf[ChildBenefitEntitlementConnector]

  val mockHttpClient: HttpClientV2      = mock[HttpClientV2]
  val mockAppConfig:  FrontendAppConfig = mock[FrontendAppConfig]
  val requestBuilder: RequestBuilder    = mock[RequestBuilder]
  val sutWithMocks: DefaultChildBenefitEntitlementConnector =
    new DefaultChildBenefitEntitlementConnector(mockHttpClient, mockAppConfig)

  def connector: ChildBenefitEntitlementConnector = {
    val injector: Injector = GuiceApplicationBuilder()
      .overrides(
        inject.bind[RequestBuilder].toInstance(requestBuilder),
        inject.bind[HttpClientV2].toInstance(mockHttpClient)
      )
      .injector()

    injector.instanceOf[ChildBenefitEntitlementConnector]
  }

  val message = "error message"

  val errors: Seq[Int] = Seq(
    UNAUTHORIZED,
    NOT_FOUND,
    INTERNAL_SERVER_ERROR,
    SERVICE_UNAVAILABLE
  )

  val fullAddress: FullAddress = FullAddress(
    addressLine1 = AddressLine("address line 1"),
    addressLine2 = AddressLine("address line 2"),
    addressLine3 = None,
    addressLine4 = None,
    addressLine5 = None,
    addressPostcode = AddressPostcode("PC00 0PC")
  )
  val claimant: Claimant = Claimant(
    name = FullName("A Name"),
    awardValue = 1111.1,
    awardStartDate = LocalDate.now().minusYears(1),
    awardEndDate = LocalDate.now().plusYears(1),
    higherRateValue = 888.8,
    standardRateValue = 222.2,
    lastPaymentsInfo = Seq.empty[LastPaymentFinancialInfo],
    fullAddress = fullAddress,
    adjustmentInformation = None
  )
  val childBenefitEntitlement: ChildBenefitEntitlement = ChildBenefitEntitlement(
    claimant = claimant,
    entitlementDate = LocalDate.now().minusDays(1),
    paidAmountForEldestOrOnlyChild = 111.11,
    paidAmountForEachAdditionalChild = 222.22,
    children = List.empty[Child]
  )

  "getChildBenefitEntitlement" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the ChildBenefitEntitlement in the response is returned" in {
        entitlementsAndPaymentHistoryStub(childBenefitEntitlement)

        whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
          result mustBe Right(childBenefitEntitlement)
        }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "THEN a ConnectorError with the matched status and description is returned" in {
        val expectedMessage = "Unit Test other failure expected message"
        entitlementsAndPaymentHistoryFailureStub(
          INTERNAL_SERVER_ERROR,
          genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
        )

        whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
          result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
        }
      }
    }

    "GIVEN the HttpClient experiences an exception" - {
      "AND the exception is of type HttpException" - {
        "THEN a ConnectorError is returned with the matching details" in {
          when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

          when(
            requestBuilder.execute[Either[CBError, ChildBenefitEntitlement]](
              any[HttpReads[Either[CBError, ChildBenefitEntitlement]]],
              any[ExecutionContext]
            )
          )
            .thenReturn(Future.successful(Left(ConnectorError(SERVICE_UNAVAILABLE, message))))

          whenReady(connector.getChildBenefitEntitlement.value) { result =>
            result mustBe Left(ConnectorError(SERVICE_UNAVAILABLE, message))
          }
        }
      }
      "AND the exception is of type UpstreamErrorResponse" - {
        "THEN a ConnectorError is returned with the matching details" in {
          when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

          when(
            requestBuilder.execute[Either[CBError, ChildBenefitEntitlement]](
              any[HttpReads[Either[CBError, ChildBenefitEntitlement]]],
              any[ExecutionContext]
            )
          )
            .thenReturn(Future.failed(UpstreamErrorResponse(message, SERVICE_UNAVAILABLE)))

          whenReady(connector.getChildBenefitEntitlement.value) { result =>
            result mustBe Left(ConnectorError(SERVICE_UNAVAILABLE, message))
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        entitlementsAndPaymentHistoryFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
          result mustBe a[Left[_, ChildBenefitEntitlement]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
    }
    }
    "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
      "THEN an expected ConnectorError is returned" in {
        entitlementsAndPaymentHistoryFailureStub(OK, validNotMatchingJsonResponse)

        whenReady(sutWithStubs.getChildBenefitEntitlement.value) { result =>
          result mustBe a[Left[_, ChildBenefitEntitlement]]
          result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
        }
      }
    }
  }

  "companion object: DefaultChildBenefitEntitlementConnector" - {
    "logMessage" - {
      "GIVEN a status code and a message" - {
        "THEN the returned log message contains both" in {
          val result = DefaultChildBenefitEntitlementConnector.logMessage(SERVICE_UNAVAILABLE, message)

          result must include(SERVICE_UNAVAILABLE.toString)
          result must include(message)
        }
      }
    }
  }
}
