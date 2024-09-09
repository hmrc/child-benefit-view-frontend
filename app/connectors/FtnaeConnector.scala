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

import cats.data.EitherT
import cats.syntax.either._
import config.FrontendAppConfig
import connectors.FtnaeConnector.{claimantInfoLogMessage, mainError}
import models.CBEnvelope.CBEnvelope
import models.errors._
import models.ftnae.{ChildDetails, FtnaeResponse}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps, UpstreamErrorResponse}
import utils.helpers.Implicits._
import utils.logging.RequestLogger

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

@Singleton
class FtnaeConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig)
    extends HttpReadsWrapper[CBErrorResponse] {

  private val logger = new RequestLogger(this.getClass)

  def getFtnaeAccountDetails()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[FtnaeResponse] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .get(url"${appConfig.getFtnaeAccountInfoUrl}")
          .execute
          .recover {
            case e: HttpException =>
              logger.error(claimantInfoLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[FtnaeResponse]
            case e: UpstreamErrorResponse =>
              logger.error(claimantInfoLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[FtnaeResponse]
          }
      )
    }

  def uploadFtnaeDetails(childDetails: ChildDetails)(implicit
      ec:                              ExecutionContext,
      hc:                              HeaderCarrier
  ): CBEnvelope[Unit] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .put(url"${appConfig.updateFtnaeInfoUrl}")
          .withBody(Json.toJson(childDetails))
          .execute
          .recover {
            case e: HttpException =>
              logger.error(claimantInfoLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[Unit]
            case e: UpstreamErrorResponse =>
              logger.error(claimantInfoLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[Unit]
          }
      )
    }

  private def extractMainError(message: String): String = mainError.findFirstIn(message).fold(message)(identity)

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: CBErrorResponse): CBError = {
    val extractedMainErrorMessage = extractMainError(upstreamError.description)
    status match {
      case Status.NOT_FOUND if extractedMainErrorMessage.toUpperCase().trim.equals("NO CHB ACCOUNT") =>
        FtnaeNoCHBAccountError

      case Status.NOT_FOUND if extractedMainErrorMessage.toUpperCase().trim.startsWith("CAN NOT FIND YOUNG PERSON") =>
        FtnaeCannotFindYoungPersonError

      case _ => ConnectorError(status, upstreamError.description)
    }
  }

}

object FtnaeConnector {
  val claimantInfoLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Ftnae Response: code=$code message=$message"

  private val mainError: Regex = """(?<=\[).+?(?=\])""".r
}
