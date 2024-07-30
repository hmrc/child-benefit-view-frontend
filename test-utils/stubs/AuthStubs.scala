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

package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, stubFor, unauthorized, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object AuthStubs {
  val childBenefitRetrievals: String =
    """{
              |  "authorise" : [ {
              |    "$or" : [ {
              |      "affinityGroup" : "Individual"
              |    }, {
              |      "affinityGroup" : "Organisation"
              |    } ]
              |  }, {
              |    "authProviders" : [ "GovernmentGateway" ]
              |  }, {
              |    "confidenceLevel" : 200
              |  } ],
              |  "retrieve" : [ "nino", "internalId", "allEnrolments" ]
              |}""".stripMargin('|')

  def userLoggedInIsChildBenefitUser(testUserJson: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(childBenefitRetrievals))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(testUserJson)
        )
    )

  def userLoggedInIsNotChildBenefitUser(error: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .withRequestBody(equalToJson(childBenefitRetrievals))
        .willReturn(
          unauthorized.withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
        )
    )

  def userNotLoggedIn(): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          unauthorized.withHeader("WWW-Authenticate", s"""MDTP detail="Session record not found"""")
        )
    )
}
