/*
 * Copyright 2022 HM Revenue & Customs
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
import models.changeofbank.ClaimantBankInformation
import models.errors.{CBError, CBErrorResponse, ClaimantIsLockedOutOfChangeOfBank, ConnectorError}
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.{ForbiddenException, HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ChangeOfBankConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)
    extends HttpReadsWrapper[CBErrorResponse]
    with Logging {

  private val claimantInfoLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank User Info: code=$code message=$message"

  private val claimantVerificationLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank bank account verification: code=$code message=$message"

  def getChangeOfBankClaimantInfo(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ClaimantBankInformation] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(appConfig.changeOfBankUserInfoUrl)(httpReads, hc, ec)
          .recover {
            case e: HttpException =>
              logger.error(claimantInfoLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[ClaimantBankInformation]
            case e: UpstreamErrorResponse =>
              logger.error(claimantInfoLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[ClaimantBankInformation]
          }
      )
    }

  //TODO - won't be a Boolean but this will be addressed in another ticket
  def verifyClaimantBankAccount(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[Boolean] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(appConfig.verifyBankAccountUrl)(httpReads, hc, ec)
          .recover {
            case e: ForbiddenException if e.message.contains("ClaimantIsLockedOut") =>
              logger.debug(claimantVerificationLogMessage(e.responseCode, e.getMessage))
              ClaimantIsLockedOutOfChangeOfBank(e.responseCode).asLeft[Boolean]
            case e: HttpException =>
              logger.error(claimantVerificationLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[Boolean]
            case e: UpstreamErrorResponse =>
              logger.error(claimantVerificationLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[Boolean]
          }
      )
    }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: CBErrorResponse): CBError =
    status match {
      case Status.FORBIDDEN if upstreamError.description.contains("ClaimantIsLockedOut") =>
        ClaimantIsLockedOutOfChangeOfBank(Status.FORBIDDEN)
      case _ => ConnectorError(status, upstreamError.description)
    }

}
