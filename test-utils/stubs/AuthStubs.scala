package stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, serverError, stubFor, unauthorized, urlEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object AuthStubs {
  val childBenefitRetrievals: String =
    """
      |{
      |	"authorise": [{
      |		"authProviders": ["GovernmentGateway"]
      |	}],
      |	"retrieve": ["nino", "affinityGroup", "internalId", "confidenceLevel"]
      |}
      |""".stripMargin

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

  def userNotLoggedIn(error: String): StubMapping =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          serverError.withHeader("WWW-Authenticate", s"""MDTP detail="$error"""")
        )
    )
}
