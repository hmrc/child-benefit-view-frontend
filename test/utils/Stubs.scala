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

package utils

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.changeofbank.ClaimantBankInformation
import models.entitlement.ChildBenefitEntitlement
import play.api.libs.json.Json

object Stubs {

  private val ChildBenefitRetrievals: String =
    """
      |{
      |	"authorise": [{
      |		"authProviders": ["GovernmentGateway"]
      |	}],
      |	"retrieve": ["nino", "affinityGroup", "internalId", "confidenceLevel"]
      |}
      |""".stripMargin

  def userLoggedInChildBenefitUser(testUserJson: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(ChildBenefitRetrievals))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(testUserJson)
        )
    )

  def userNotLoggedIn(error: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
//        .withRequestBody(equalToJson(ChildBenefitRetrievals))
        .willReturn(
          serverError.withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
        )
    )

  def userLoggedInIsNotChildBenefitUser(error: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(ChildBenefitRetrievals))
        .willReturn(
          unauthorized.withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
        )
    )

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

  def entitlementsAndPaymentHistoryFailureStub(result: String, status: Int = 200): StubMapping =
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

  def changeOfBankUserInfoFailureStub(status: Int = 404, result: String): StubMapping =
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
      get(urlEqualTo("/child-benefit-service/verify-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(true).toString)
        )
    )

  def verifyClaimantBankInfoFailureStub(status: Int = 403, result: String): StubMapping =
    stubFor(
      get(urlEqualTo("/child-benefit-service/verify-claimant-bank-account"))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(result)
        )
    )
}
