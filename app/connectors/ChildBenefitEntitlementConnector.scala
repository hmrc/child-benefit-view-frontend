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
import cats.implicits.catsSyntaxEitherId
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import models.CBEnvelope
import models.entitlement.ChildBenefitEntitlement
import models.errors.ConnectorError
import models.failure.Failures
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}
import util.FileUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[DefaultChildBenefitEntitlementConnector])
trait ChildBenefitEntitlementConnector {
  def getChildBenefitEntitlement(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ChildBenefitEntitlement]
}

@Singleton
class MockChildBenefitEntitlementConnector extends ChildBenefitEntitlementConnector {
  override def getChildBenefitEntitlement(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ChildBenefitEntitlement] =
    EitherT(
      Future(
        for {
          content <-
            Try(FileUtils.readContent("entitlement", "LizJones")).toEither.left
              .map(e => s"Cannot read entitlement file: $e")

          json <-
            Try(Json.parse(content)).toEither.left
              .map(e => s"Cannot parse entitlement json: $e")

          entitlement <-
            Try(json.as[ChildBenefitEntitlement]).toEither.left
              .map(e => s"Invalid entitlement json: $e")
        } yield {
          entitlement
        }
      )
    ).leftMap { error =>
      ConnectorError(Status.INTERNAL_SERVER_ERROR, error)
    }
}

@Singleton
class DefaultChildBenefitEntitlementConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)
    extends ChildBenefitEntitlementConnector
    with HttpReadsWrapper[ChildBenefitEntitlement, Failures] {
  def getChildBenefitEntitlement(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ChildBenefitEntitlement] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(appConfig.childBenefitEntitlementUrl)(httpReads, hc, ec)
          .recover {
            case e: HttpException => ConnectorError(e.responseCode, e.getMessage).asLeft[ChildBenefitEntitlement]
            case e: UpstreamErrorResponse =>
              ConnectorError(e.statusCode, e.getMessage).asLeft[ChildBenefitEntitlement]
          }
      )
    }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: Failures): ConnectorError =
    ConnectorError(status, upstreamError.failures.map(f => s"code: ${f.code}. reason: ${f.reason}").mkString(";"))
}
