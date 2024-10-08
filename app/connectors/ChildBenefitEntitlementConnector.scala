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
import cats.implicits.catsSyntaxEitherId
import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import connectors.DefaultChildBenefitEntitlementConnector.logMessage
import models.CBEnvelope.CBEnvelope
import models.entitlement.ChildBenefitEntitlement
import models.errors.{CBError, CBErrorResponse, ConnectorError}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps, UpstreamErrorResponse}
import utils.logging.RequestLogger

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[DefaultChildBenefitEntitlementConnector])
trait ChildBenefitEntitlementConnector {
  def getChildBenefitEntitlement(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ChildBenefitEntitlement]
}

@Singleton
class DefaultChildBenefitEntitlementConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig)
    extends ChildBenefitEntitlementConnector
    with HttpReadsWrapper[CBErrorResponse] {

  private val logger = new RequestLogger(this.getClass)

  def getChildBenefitEntitlement(implicit
      ec: ExecutionContext,
      hc: HeaderCarrier
  ): CBEnvelope[ChildBenefitEntitlement] =
    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .get(url"${appConfig.childBenefitEntitlementUrl}")
          .execute[Either[CBError, ChildBenefitEntitlement]]
          .recover {
            case e: HttpException =>
              logger.error(logMessage(e.responseCode, e.getMessage))
              ConnectorError(e.responseCode, e.getMessage).asLeft[ChildBenefitEntitlement]
            case e: UpstreamErrorResponse =>
              logger.error(logMessage(e.statusCode, e.getMessage))
              ConnectorError(e.statusCode, e.getMessage).asLeft[ChildBenefitEntitlement]
          }
      )
    }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: CBErrorResponse): ConnectorError =
    ConnectorError(status, upstreamError.description)
}

object DefaultChildBenefitEntitlementConnector {
  val logMessage = (code: Int, message: String) =>
    s"unable to retrieve Child Benefit Entitlement: code=$code message=$message"
}
