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
import models.CBEnvelope.CBEnvelope
import models.errors._
import models.ftnae.FtneaResponse
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsSuccess, Reads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

@Singleton
class FtneaConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)
    extends HttpReadsWrapper[CBErrorResponse]
    with Logging {

  private val claimantInfoLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Ftnea Response: code=$code message=$message"

  private val mainError: Regex = """(?<=\[).+?(?=\])""".r
  def getFtneaAccountDetails()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[FtneaResponse] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(appConfig.getFtneaAccountInfoUrl)(httpReads, hc, ec)
          .recover {
            case e: HttpException =>
              logger.error(claimantInfoLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[FtneaResponse]
            case e: UpstreamErrorResponse =>
              logger.error(claimantInfoLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[FtneaResponse]
          }
      )
    }

  private def extractMainError(message: String): String = mainError.findFirstIn(message).fold(message)(identity)

  implicit val reads: Reads[Unit] = Reads[Unit] { _ =>
    JsSuccess(())
  }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: CBErrorResponse): CBError = {
    val extractedMainErrorMessage = extractMainError(upstreamError.description)
    status match {
      case Status.NOT_FOUND if extractedMainErrorMessage.toUpperCase().trim.equals("NO CHB ACCOUNT") =>
        FtneaNoCHBAccountError

      case Status.NOT_FOUND if extractedMainErrorMessage.toUpperCase().trim.startsWith("CAN NOT FIND YOUNG PERSON") =>
        FtneaCannotFindYoungPersonError

      case _ => ConnectorError(status, upstreamError.description)
    }
  }

}
