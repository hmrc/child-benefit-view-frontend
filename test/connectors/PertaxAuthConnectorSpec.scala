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

package connectors

import base.BaseAppSpec
import models.pertaxAuth.PertaxAuthResponseModel
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.BAD_REQUEST
import play.api.test.Helpers.running
import play.api.test.Injecting
import play.twirl.api.Html
import stubs.AuthStubs.{mockPertaxPartial, mockPostPertaxAuth}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.partials.HtmlPartial

import scala.concurrent.{ExecutionContext, Future}

class PertaxAuthConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite with Injecting {

  override implicit lazy val app: Application = applicationBuilder().build()

  lazy val connector:         PertaxAuthConnector = inject[PertaxAuthConnector]
  implicit val headerCarrier: HeaderCarrier       = HeaderCarrier()
  implicit lazy val ec:       ExecutionContext    = app.injector.instanceOf[ExecutionContext]

  val nino = "AA000000A"

  "PertaxAuthConnector" - {
    "calling .pertaxPostAuthorise" - {
      "pertax returns a successful response" - {
        "return the expected return model" in {
          val expectedReturnModel = PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None)

          mockPostPertaxAuth(expectedReturnModel)

          val result = connector.pertaxPostAuthorise

          result.map { res =>
            res mustEqual expectedReturnModel
          }
        }
      }
    }

    "calling .loadPartial" - {
      "pertax returns a successful response" - {
        "return an HtmlPartial" in {
          val expectedPartial = HtmlPartial.Success(Some("Test Page"), Html("<html></html>"))
          mockPertaxPartial("""<html></html>""", Some("Test Page"))

          val result = connector.loadPartial("partial")

          await(result) mustEqual expectedPartial
        }
      }

      "pertax returns an unsuccessful response" - {
        "return a failure partial" in {
          val expectedPartial = HtmlPartial.Failure(Some(BAD_REQUEST), "<html>Failure Page</html>")

          val result = {
            mockPertaxPartial("<html>Failure Page</html>", None, BAD_REQUEST)
            connector.loadPartial("partial")
          }

          await(result) mustEqual expectedPartial
        }
      }

      "ensure url has leading /" - {
        "check char at 0" in {
          val expectedPartial = HtmlPartial.Failure(Some(BAD_REQUEST), "<html>Failure Page</html>")

          val result1 = {
            mockPertaxPartial("<html>Failure Page</html>", None, BAD_REQUEST)
            connector.loadPartial("/partial")
          }

          val result2 = {
            mockPertaxPartial("<html>Failure Page</html>", None, BAD_REQUEST)
            connector.loadPartial("partial")
          }

          await(result1) mustEqual expectedPartial
          await(result2) mustEqual expectedPartial
        }
      }
    }
  }
}
