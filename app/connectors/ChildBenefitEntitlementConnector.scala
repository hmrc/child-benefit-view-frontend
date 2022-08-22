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
import models.CBEnvelope.CBEnvelope
import models.entitlement.ChildBenefitEntitlement
import models.errors.ConnectorError
import models.failure.Failures
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, UpstreamErrorResponse}
import util.FileUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@ImplementedBy(classOf[MockRelationshipDetailsConnector])
trait ChildBenefitEntitlementConnector {
  def getChildBenefitEntitlementUrl()(implicit
                              ec: ExecutionContext,
                              hc: HeaderCarrier
                            ): CBEnvelope[ChildBenefitEntitlement]
}

@Singleton
class MockRelationshipDetailsConnector @Inject() ()  extends ChildBenefitEntitlementConnector {
  override def getChildBenefitEntitlementUrl()(implicit ec: ExecutionContext, hc: HeaderCarrier): CBEnvelope[ChildBenefitEntitlement] = {

      val content  = FileUtils.readContent("entitlement")
      val json = Json.parse(content)
      val childBenefitEntitlement : ChildBenefitEntitlement =json.as[ChildBenefitEntitlement]
      CBEnvelope(childBenefitEntitlement)
  }
}

@Singleton
class DefaultRelationshipDetailsConnector @Inject() (httpClient: HttpClient, appConfig: FrontendAppConfig)
  extends ChildBenefitEntitlementConnector
    with HttpReadsWrapper[ChildBenefitEntitlement, Failures] {
  def getChildBenefitEntitlementUrl()(implicit
                              ec: ExecutionContext,
                              hc: HeaderCarrier
                            ): CBEnvelope[ChildBenefitEntitlement] = {
    val url =
      s"${appConfig.childBenefitEntitlementUrl}${}"

    withHttpReads { implicit httpReads =>
      EitherT(
        httpClient
          .GET(url)(httpReads, hc, ec)
          .recover {
            case e: HttpException => ConnectorError(e.responseCode, e.getMessage).asLeft[ChildBenefitEntitlement]
            case e: UpstreamErrorResponse =>
              ConnectorError(e.statusCode, e.getMessage).asLeft[ChildBenefitEntitlement]
          }
      )
    }
  }

  override def fromUpstreamErrorToCBError(status: Int, upstreamError: Failures): ConnectorError =
    ConnectorError(status, upstreamError.failures.map(f => s"code: ${f.code}. reason: ${f.reason}").mkString(";"))
}
