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

import base.BaseSpec
import uk.gov.hmrc.auth.core._

class HmrcPTChecksSpec extends BaseSpec {

  val enrolmentChecks = new HmrcPTChecks
  val nino = "AA000000A"

  "isHmrcPTEnrolmentPresentAndValid" - {
    "must return false when No HMRC-PT enrolment is present" in {
      val result = enrolmentChecks.isHmrcPTEnrolmentPresentAndValid(nino, Enrolments(Set.empty))
      result mustBe false
    }

    "must return false when nino does not match" in {
      val result = enrolmentChecks.isHmrcPTEnrolmentPresentAndValid(nino, Enrolments(Set.empty))
      result mustBe false
    }

    "must return true when enrolment is present and valid" in {
      val result = enrolmentChecks.isHmrcPTEnrolmentPresentAndValid(nino, Enrolments(
        Set(Enrolment("HMRC-PT", Seq(EnrolmentIdentifier("NINO", nino)), "activated"))
      ))
      result mustBe true
    }
  }
}
