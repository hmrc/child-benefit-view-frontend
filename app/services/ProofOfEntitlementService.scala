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

import models.entitlement.ChildBenefitEntitlement
import play.api.libs.json.Json
import util.FileUtils

import scala.concurrent.{ExecutionContext, Future}

class ProofOfEntitlementService {

  def getEntitlement() (implicit ec : ExecutionContext): Future[ChildBenefitEntitlement] =


    for {
      content <- Future(FileUtils.readContent("entitlement"))
      json <- Future(Json.parse(content))
      entitlement <- Future(json.as[ChildBenefitEntitlement])
    } yield {
      entitlement
    }


  //val connector = new ChildBenefitEntitlementConnector(httpClient, appConfig)

}
