package connectors

import base.BaseAppSpec
import models.entitlement.ChildBenefitEntitlement
import models.errors.ConnectorError
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestData.genericCBError

import scala.concurrent.ExecutionContext

class ChildBenefitEntitlementConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val executionContext = app.injector.instanceOf[ExecutionContext]
  implicit val headerCarrier = new HeaderCarrier()

  override implicit lazy val app = applicationBuilder().build()
  val sut = app.injector.instanceOf[ChildBenefitEntitlementConnector]

  "ChildBenefitEntitlementConnector" - {
    "getChildBenefitEntitlement" - {
      "GIVEN the HttpClient receives a successful response" - {
        "THEN the ChildBenefitEntitlement in the response is returned" in {
          forAll(arbitrary[ChildBenefitEntitlement]) { childBenefitEntitlement =>
            entitlementsAndPaymentHistoryStub(childBenefitEntitlement)

            whenReady(sut.getChildBenefitEntitlement.value) { result =>
              result mustBe Right(childBenefitEntitlement)
            }
          }
        }
      }
      "GIVEN the HttpClient receives a CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          entitlementsAndPaymentHistoryFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

          whenReady(sut.getChildBenefitEntitlement.value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }
  }
}
