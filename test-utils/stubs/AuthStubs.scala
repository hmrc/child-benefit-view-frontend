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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.pertaxAuth.PertaxAuthResponseModel
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.utils.UriEncoding

object AuthStubs {
  val childBenefitRetrievals: String =
    """{
              |  "authorise" : [],
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

  def mockPostPertaxAuth(returnedValue: PertaxAuthResponseModel): StubMapping = {
    stubFor(
      post(urlEqualTo("/pertax/authorise"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.stringify(Json.toJson(returnedValue)))
        )
    )
  }

  def mockPertaxPartial(
      body:       String,
      title:      Option[String],
      status:     Int = OK,
      partialUrl: String = "/partial"
  ): StubMapping = {
    val response = aResponse()
      .withStatus(status)
      .withBody(body)

    stubFor(
      get(urlEqualTo(partialUrl))
        .willReturn(
          title.fold(response) { unwrappedTitle =>
            response.withHeader("X-Title", UriEncoding.encodePathSegment(unwrappedTitle, "UTF-8"))
          }
        )
    )
  }
}
