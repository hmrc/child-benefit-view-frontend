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

package services

import audit.ViewProofOfEntitlementModel
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify}
import org.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

class AuditorTest extends PlaySpec {

  val auditConnector:        AuditConnector   = mock[AuditConnector]
  val auditor:               Auditor          = new Auditor(auditConnector)
  protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()
  val someId:                String           = "failing testId"

  "Auditor" should {

    "fire -event message-" when {

      "viewProofOfEntitlement is called" in {

        Mockito.reset(auditConnector)

        val captor = ArgumentCaptor.forClass(classOf[ViewProofOfEntitlementModel])

        auditor.viewProofOfEntitlement

        verify(auditConnector, times(1))
          .sendExplicitAudit(eqTo(ViewProofOfEntitlementModel.eventType), captor.capture())(any(), any(), any())

        val capturedEvent = captor.getValue.asInstanceOf[ViewProofOfEntitlementModel]
        capturedEvent.nino mustBe "AB123445A"
      }
    }
  }
}
