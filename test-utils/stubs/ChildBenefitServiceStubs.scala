package stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.changeofbank.ClaimantBankInformation
import models.cob.UpdateBankDetailsResponse
import models.entitlement.ChildBenefitEntitlement
import models.ftnae.{ChildDetails, FtnaeResponse}
import play.api.http.Status.{OK}
import play.api.libs.json.{Json, Writes}

object ChildBenefitServiceStubs {

  def verifyClaimantBankAccountStub(): StubMapping =
    putSuccessStub("/child-benefit-service/verify-claimant-bank-account")
  def verifyClaimantBankAccountFailureStub(status: Int, result: String) : StubMapping =
    putFailureStub("/child-benefit-service/verify-claimant-bank-account", status, result)

  def entitlementsAndPaymentHistoryStub(result: ChildBenefitEntitlement): StubMapping =
    getSuccessStub("/child-benefit-service/view-entitlements-and-payments", result)
  def entitlementsAndPaymentHistoryFailureStub(status: Int, result: String): StubMapping =
    getFailureStub("/child-benefit-service/view-entitlements-and-payments", status, result)

  def changeOfBankUserInfoStub(result: ClaimantBankInformation): StubMapping =
    getSuccessStub("/child-benefit-service/change-bank-user-information", result)
  def changeOfBankUserInfoFailureStub(status: Int, result: String): StubMapping =
    getFailureStub("/child-benefit-service/change-bank-user-information", status, result)

  def verifyClaimantBankInfoStub(): StubMapping =
    putSuccessStub("/child-benefit-service/verify-claimant-bank-account")
  def verifyClaimantBankInfoFailureStub(status: Int, result: String): StubMapping =
    putFailureStub("/child-benefit-service/verify-claimant-bank-account", status, result)

  def verifyBARNotLockedStub(): StubMapping =
    getSuccessStub("/child-benefit-service/verify-bar-not-locked")
  def verifyBARNotLockedFailureStub(status: Int, result: String): StubMapping =
    getFailureStub("/child-benefit-service/verify-bar-not-locked", status, result)

  def updateBankAccountStub(result: UpdateBankDetailsResponse): StubMapping =
    putSuccessStub("/child-benefit-service/update-claimant-bank-account", result)
  def updateBankAccountFailureStub(status: Int, result: String): StubMapping =
    putFailureStub("/child-benefit-service/update-claimant-bank-account", status, result)

  def getFtnaeAccountDetailsStub(result: FtnaeResponse): StubMapping =
    getSuccessStub("/child-benefit-service/retrieve-ftnae-account-information", result)
  def getFtnaeAccountDetailsFailureStub(status: Int, result: String): StubMapping =
    getFailureStub("/child-benefit-service/retrieve-ftnae-account-information", status, result)

  def uploadFtnaeDetailsStub(result: ChildDetails) =
    putSuccessStub("/child-benefit-service/update-child-information/ftnae", result)
  def uploadFtnaeDetailsFailureStub(status: Int, result: String): StubMapping =
    putFailureStub("/child-benefit-service/update-child-information/ftnae", status, result)

  def dropChangeOfBankStub(): StubMapping =
    deleteSuccessStub("/child-benefit-service/drop-change-bank-cache")
  def dropChangeOfBankFailureStub(status: Int, result: String): StubMapping =
    deleteFailureStub("/child-benefit-service/drop-change-bank-cache", status, result)

  private def getSuccessStub(url: String): StubMapping = getSuccessStub(url, true)
  private def getSuccessStub[T](url: String, result: T)(implicit writes: Writes[T]): StubMapping =
    stubFor(
      get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.toJson(result).toString)
        )
    )
  private def getFailureStub(url: String, status: Int, result: String) =
    stubFor(
      get(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  private def putSuccessStub(url: String): StubMapping = putSuccessStub(url, OK, true)
  private def putSuccessStub[T](url: String, result: T)(implicit writes: Writes[T]): StubMapping =
    putSuccessStub(url, OK, result)
  private def putSuccessStub[T](url: String, status: Int, result: T)(implicit writes: Writes[T]): StubMapping =
    stubFor(
      put(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(Json.toJson(result).toString)
        )
    )
  private def putFailureStub(url: String, status: Int, result: String) =
    stubFor(
      put(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )

  private def deleteSuccessStub(url: String): StubMapping = deleteSuccessStub(url, true)
  private def deleteSuccessStub[T](url: String, result: T)(implicit writes: Writes[T]): StubMapping =
    stubFor(
      delete(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.toJson(result).toString)
        )
    )
  private def deleteFailureStub(url: String, status: Int, result: String) =
    stubFor(
      delete(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )
}
