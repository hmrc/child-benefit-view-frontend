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

package controllers.actions

import base.BaseAppSpec
import cats.data.EitherT
import config.FrontendAppConfig
import connectors.PertaxAuthConnector
import models.common.NationalInsuranceNumber
import models.pertaxAuth.{PertaxAuthResponseModel, PertaxErrorView}
import models.requests.IdentifierRequest
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Play.materializer
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR, SEE_OTHER, UNAUTHORIZED}
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, RequestHeader, Result}
import play.api.test.Helpers.LOCATION
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.partials.HtmlPartial
import views.html.ErrorTemplate

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContext, Future}

class PertaxAuthActionSpec extends BaseAppSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit val headerCarrier: HeaderCarrier =
    HeaderCarrier()

  lazy val connector: PertaxAuthConnector =
    mock[PertaxAuthConnector]

  lazy val authAction = new PertaxAuthActionImpl(
    pertaxAuthConnector = connector,
    errorTemplate = app.injector.instanceOf[ErrorTemplate],
    appConfig = app.injector.instanceOf[FrontendAppConfig]
  )(ExecutionContext.Implicits.global, Helpers.stubMessagesControllerComponents())

  lazy val date    = LocalDate.now()
  lazy val instant = Instant.now()

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset()
  }

  def authenticatedRequest(requestMethod: String = "GET", requestUrl: String = "/"): IdentifierRequest[AnyContent] =
    new IdentifierRequest[AnyContent](
      FakeRequest(requestMethod, requestUrl),
      NationalInsuranceNumber("AA000000A"),
      true,
      ""
    )

  def mockAuth(
      pertaxAuthResponseModel: PertaxAuthResponseModel
  ): OngoingStubbing[EitherT[Future, UpstreamErrorResponse, PertaxAuthResponseModel]] = {
    when(connector.pertaxPostAuthorise(any(), any())).thenReturn(
      EitherT[Future, UpstreamErrorResponse, PertaxAuthResponseModel](Future.successful(Right(pertaxAuthResponseModel)))
    )
  }

  def mockFailedAuth(
      error: UpstreamErrorResponse
  ): OngoingStubbing[EitherT[Future, UpstreamErrorResponse, PertaxAuthResponseModel]] = {
    when(connector.pertaxPostAuthorise(any(), any()))
      .thenReturn(EitherT[Future, UpstreamErrorResponse, PertaxAuthResponseModel](Future.successful(Left(error))))
  }

  def block: RequestHeader => Future[Result] = _ => Future.successful(Ok("Successful"))

  "PertaxAuthAction.filter" - {

    "the pertax auth feature switch is on" - {

      "return Successful" - {

        "the response from pertax auth connector indicates ACCESS_GRANTED" in {
          val result = {
            mockAuth(
              PertaxAuthResponseModel(
                "ACCESS_GRANTED",
                "This message doesn't matter.",
                None,
                None
              )
            )

            authAction.invokeBlock(authenticatedRequest(), block)
          }

          await(bodyOf(result)) mustEqual "Successful"
        }
      }

      "redirect the user" - {

        "the response from pertax auth connector has an UNAUTHORIZED status" in {
          lazy val result = await({
            mockFailedAuth(UpstreamErrorResponse("Error", UNAUTHORIZED))
            authAction.invokeBlock(authenticatedRequest(), block)
          })

          result.header.status mustEqual SEE_OTHER
        }

        "the response from pertax auth connector indicates NO_HMRC_PT_ENROLMENT and has a redirect URL" - {
          lazy val request = {
            mockAuth(
              PertaxAuthResponseModel(
                "NO_HMRC_PT_ENROLMENT",
                "Still doesn't matter.",
                Some("/some-redirect"),
                None
              )
            )

            authAction.invokeBlock(authenticatedRequest(requestUrl = "/some-base-url"), block)
          }
          lazy val result = await(request)

          "has a status of SEE_OTHER(303)" in {
            status(result) mustEqual SEE_OTHER
          }

          "has a redirect url of '/some-redirect'" in {
            val expectedRedirectUrl = "/some-redirect?redirectUrl=%2Fsome-base-url"

            result.header.headers(LOCATION) mustEqual expectedRedirectUrl
          }

          "the response from pertax auth connector indicates CREDENTIAL_STRENGTH_UPLIFT_REQUIRED" - {
            lazy val request = {
              mockAuth(
                PertaxAuthResponseModel(
                  "CREDENTIAL_STRENGTH_UPLIFT_REQUIRED",
                  "Still doesn't matter.",
                  Some("/some-redirect"),
                  None
                )
              )

              authAction.invokeBlock(authenticatedRequest(requestUrl = "/some-base-url"), block)
            }
            lazy val result = await(request)

            "has a status of INTERNAL_SERVER_ERROR(500)" in {
              status(result) mustEqual INTERNAL_SERVER_ERROR
            }
          }

          "the response from pertax auth connector indicates CONFIDENCE_LEVEL_UPLIFT_REQUIRED and has a redirect URL" - {
            lazy val request = {
              mockAuth(
                PertaxAuthResponseModel(
                  "CONFIDENCE_LEVEL_UPLIFT_REQUIRED",
                  "Still doesn't matter.",
                  Some("/some-redirect"),
                  None
                )
              )

              authAction.invokeBlock(authenticatedRequest(requestUrl = "/some-base-url"), block)
            }
            lazy val result = await(request)

            "has a status of SEE_OTHER(303)" in {
              status(result) mustEqual SEE_OTHER
            }
          }
        }
      }

      "return an error and a partial returned from the pertax auth service" - {

        "the pertax auth service returns a partial" - {

          lazy val result = await({
            mockAuth(
              PertaxAuthResponseModel(
                "NOT_A_VALID_CODE",
                "Doesn't matter, even now.",
                None,
                Some(PertaxErrorView(IM_A_TEAPOT, "/partial-url"))
              )
            )

            when(connector.loadPartial(any())(any())).thenReturn(
              Future.successful(
                HtmlPartial.Success(Some("Test Title"), Html("<div id=\"partial\">Hello</div>"))
              )
            )

            authAction.invokeBlock(authenticatedRequest(), block)
          })

          "has a status of IM_A_TEAPOT(418)" in {
            result.header.status mustEqual IM_A_TEAPOT
          }

          "has a partial of 'Hello'" in {
            lazy val doc = Jsoup.parse(bodyOf(result))
            doc.getElementById("partial").text() mustEqual "Hello"
          }
        }
      }

      "return an internal server error" - {

        "pertax auth connector returns a left that isn't unauthorised" in {
          lazy val result = await({
            mockFailedAuth(UpstreamErrorResponse("Error", INTERNAL_SERVER_ERROR))
            authAction.invokeBlock(authenticatedRequest(), block)
          })

          result.header.status mustEqual INTERNAL_SERVER_ERROR
        }

        "the pertax auth service fails to return a partial" - {

          "has a status of INTERNAL_SERVER_ERROR(500)" in {
            lazy val result = await({
              mockAuth(
                PertaxAuthResponseModel(
                  "NOT_A_VALID_CODE",
                  "Doesn't matter, even now.",
                  None,
                  Some(PertaxErrorView(IM_A_TEAPOT, "/partial-url"))
                )
              )

              when(connector.loadPartial(any())(any())).thenReturn(
                Future.successful(
                  HtmlPartial.Failure(None, "ERROR")
                )
              )

              authAction.invokeBlock(authenticatedRequest(), block)
            })

            result.header.status mustEqual INTERNAL_SERVER_ERROR
          }
        }

        "the pertax authentication fails" - {

          "has a status of INTERNAL_SERVER_ERROR(500)" in {
            lazy val result = await({
              mockFailedAuth(UpstreamErrorResponse("I'M AN ERROR", INTERNAL_SERVER_ERROR))
              authAction.invokeBlock(authenticatedRequest(), block)
            })

            result.header.status mustEqual INTERNAL_SERVER_ERROR
          }
        }
      }
    }
  }
}
