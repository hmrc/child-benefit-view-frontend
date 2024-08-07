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

package utils.enrolments

import play.api.Logging
import uk.gov.hmrc.auth.core.Enrolments

import javax.inject.Inject

class HmrcPTChecks @Inject() () extends Logging {

  def isHmrcPTEnrolmentPresentAndValid(nino: String, enrolments: Enrolments): Boolean = {
    enrolments.enrolments
      .filter(_.key == "HMRC-PT")
      .flatMap { enrolment =>
        enrolment.identifiers
          .filter(id => id.key == "NINO")
      } match {
      case enrolmentIdentifiers if enrolmentIdentifiers.isEmpty =>
        false
      case enrolmentIdentifiers
          if enrolmentIdentifiers.exists(enrolmentIdentifier => enrolmentIdentifier.value == nino) =>
        true
      case _ =>
        logger.error("The nino in HMRC-PT enrolment does not match the one from the user session")
        false
    }
  }
}
