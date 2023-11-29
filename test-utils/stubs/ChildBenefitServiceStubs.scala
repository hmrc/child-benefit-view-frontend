package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.changeofbank.ClaimantBankInformation
import models.cob.UpdateBankDetailsResponse
import models.entitlement.ChildBenefitEntitlement
import play.api.libs.json.Json

object ChildBenefitServiceStubs {

  def verifyClaimantBankAccount(status: Int, message: String): StubMapping = {
    stubFor(
      put(urlEqualTo("/child-benefit-service/verify-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(message)
        )
    )
  }

  def entitlementsAndPaymentHistoryStub(result: ChildBenefitEntitlement): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/view-entitlements-and-payments"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(result).toString)
        )
    )

  def entitlementsAndPaymentHistoryFailureStub(status: Int, result: String): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/view-entitlements-and-payments"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  def changeOfBankUserInfoStub(result: ClaimantBankInformation): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/change-bank-user-information"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(result).toString)
        )
    )

  def changeOfBankUserInfoFailureStub(status: Int, result: String): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/change-bank-user-information"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  def verifyClaimantBankInfoStub(): StubMapping =
    stubFor(
      put(urlEqualTo("/child-benefit-service/verify-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(true).toString)
        )
    )

  def verifyClaimantBankInfoFailureStub(status: Int, result: String): StubMapping =
    stubFor(
      put(urlEqualTo("/child-benefit-service/verify-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  def verifyBARNotLockedStub(): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/verify-bar-not-locked"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(true).toString)
        )
    )

  def verifyBARNotLockedFailureStub(status: Int, result: String): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/verify-bar-not-locked"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  def updateBankAccountStub(result: UpdateBankDetailsResponse): StubMapping =
    stubFor(
      put(urlEqualTo("/child-benefit-service/update-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(result).toString)
        )
    )

  def updateBankAccountFailureStub(status: Int, result: String): StubMapping =
    stubFor(
      put(urlEqualTo("/child-benefit-service/update-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )
}
