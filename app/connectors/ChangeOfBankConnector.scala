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
import models.changeofbank.ClaimantBankInformation
import models.cob.{UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.errors.{CBError, CBErrorResponse, ClaimantIsLockedOutOfChangeOfBank, ConnectorError, PriorityBARSVerificationError}
import play.api.http.Status
import play.api.libs.json.{JsSuccess, Reads}
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}
import utils.logging.RequestLogger

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

@Singleton
class ChangeOfBankConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)
    extends HttpReadsWrapper[CBErrorResponse] {

  private val logger = new RequestLogger(this.getClass)

  private val claimantInfoLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank User Info: code=$code message=$message"

  private val claimantVerificationLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank bank account verification: code=$code message=$message"

  private val barNotLockedVerificationLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank verify bar not locked verification: code=$code message=$message"

  private val claimantUpdateBankLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank update bank account: code=$code message=$message"

  private val claimantDropBankCacheLogMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Change Of Bank update bank account: code=$code message=$message"

  private val mainError: Regex = """(?<=\[).+?(?=\])""".r
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

  def verifyClaimantBankAccount(verifyBankAccountRequest: VerifyBankAccountRequest)(implicit
      ec:                                                 ExecutionContext,
      hc:                                                 HeaderCarrier
  ): CBEnvelope[Unit] = {
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .PUT(appConfig.verifyBankAccountUrl, verifyBankAccountRequest)(
            VerifyBankAccountRequest.format,
            httpReads,
            hc,
            ec
          )
          .recover {
            case e: HttpException =>
              logger.error(claimantVerificationLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[Unit]
            case e: UpstreamErrorResponse =>
              logger.error(claimantVerificationLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[Unit]
          }
      )
    }
  }

  def verifyBARNotLocked()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[Unit] = {
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(appConfig.verifyBARNotLockedUrl)(
            httpReads,
            hc,
            ec
          )
          .recover {
            case e: HttpException =>
              logger.error(barNotLockedVerificationLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[Unit]
            case e: UpstreamErrorResponse =>
              logger.error(barNotLockedVerificationLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[Unit]
          }
      )
    }
  }

  def updateBankAccount(updateBankAccountRequest: UpdateBankAccountRequest)(implicit
      ec:                                         ExecutionContext,
      hc:                                         HeaderCarrier
  ): CBEnvelope[UpdateBankDetailsResponse] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .PUT(appConfig.updateBankAccountUrl, updateBankAccountRequest)(
            UpdateBankAccountRequest.format,
            httpReads,
            hc,
            ec
          )
          .recover {
            case e: HttpException =>
              logger.error(claimantUpdateBankLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[UpdateBankDetailsResponse]
            case e: UpstreamErrorResponse =>
              logger.error(claimantUpdateBankLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[UpdateBankDetailsResponse]
          }
      )
    }

  def dropChangeOfBankCache()(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[Unit] = {
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .DELETE(appConfig.dropChangeOfBankCacheUrl)
          .recover {
            case e: HttpException =>
              logger.error(claimantDropBankCacheLogMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[Unit]
            case e: UpstreamErrorResponse =>
              logger.error(claimantDropBankCacheLogMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[Unit]
          }
      )
    }
  }


  private def extractMainError(message: String): String = mainError.findFirstIn(message).fold(message)(identity)

  implicit val reads: Reads[Unit] = Reads[Unit] { _ =>
    JsSuccess(())
  }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: CBErrorResponse): CBError = {
    val extractedMainErrorMessage = extractMainError(upstreamError.description)
    status match {
      case Status.INTERNAL_SERVER_ERROR if extractedMainErrorMessage.toUpperCase().trim.equals("BAR LOCKED") =>
        ClaimantIsLockedOutOfChangeOfBank(Status.FORBIDDEN, upstreamError.description)

      case Status.NOT_FOUND if extractedMainErrorMessage.toUpperCase().trim.startsWith("PRIORITY") =>
        PriorityBARSVerificationError(status, upstreamError.description)

      case _ => ConnectorError(status, upstreamError.description)
    }
  }

}
