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

package models.cob

import models.{Enumerable, WithMessage}
import play.api.i18n.Messages

sealed trait WhatTypeOfAccount {
  def message()(implicit messages: Messages): String
}

object WhatTypeOfAccount extends Enumerable.Implicits {

  case object Sole extends WithMessage("sole", m => m("whatTypeOfAccount.options.sole")) with WhatTypeOfAccount

  case object JointHeldByClaimant
      extends WithMessage(
        "joint_held_by_claimant",
        m => s"${m("whatTypeOfAccount.options.joint")} ${m("whatTypeOfAccount.options.jointHeldByClaimant")}"
      )
      with WhatTypeOfAccount

  case object JointNotHeldByClaimant
      extends WithMessage(
        "joint_not_held_by_claimant",
        m => s"${m("whatTypeOfAccount.options.joint")} ${m("whatTypeOfAccount.options.jointNotHeldByClaimant")}"
      )
      with WhatTypeOfAccount

  case object CreditUnion
      extends WithMessage("credit_union", m => m("whatTypeOfAccount.options.creditUnion"))
      with WhatTypeOfAccount

  val values: List[WhatTypeOfAccount] = List(Sole, JointHeldByClaimant, JointNotHeldByClaimant, CreditUnion)

  implicit val enumerable: Enumerable[WhatTypeOfAccount] =
    Enumerable(values.map(v => v.toString -> v) *)

}
