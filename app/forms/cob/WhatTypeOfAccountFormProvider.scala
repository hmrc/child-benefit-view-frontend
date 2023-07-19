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

package forms.cob

import models.cob.{WhatTypeOfAccount, AccountType, JointAccountType}
import play.api.data.Form
import utils.mappings.Mappings
import play.api.data.Forms._

import javax.inject.Inject

class WhatTypeOfAccountFormProvider @Inject() extends Mappings {

  def apply(): Form[WhatTypeOfAccount] =
    Form(
      mapping(
        AccountType.name      -> enumerable[AccountType]("whatTypeOfAccount.error.required"),
        JointAccountType.name -> optional(enumerable[JointAccountType]("whatTypeOfAccount.error.required"))
      )(bind)(unbind)
    )

  private def bind(
      accountType:    AccountType,
      heldByClaimant: Option[JointAccountType]
  ): WhatTypeOfAccount =
    (accountType, heldByClaimant) match {
      case (AccountType.Sole, _) => WhatTypeOfAccount.Sole
      case (AccountType.Joint, Some(JointAccountType.HeldByClaimant)) =>
        WhatTypeOfAccount.JointHeldByClaimant
      case (AccountType.Joint, Some(JointAccountType.NotHeldByClaimant)) =>
        WhatTypeOfAccount.JointNotHeldByClaimant
      case (_, None) => WhatTypeOfAccount.Sole
    }

  def unbind(whatAccountType: WhatTypeOfAccount): Option[(AccountType, Option[JointAccountType])] =
    whatAccountType match {
      case WhatTypeOfAccount.Sole => Some((AccountType.Sole, None))
      case WhatTypeOfAccount.JointHeldByClaimant =>
        Some((AccountType.Joint, Some(JointAccountType.HeldByClaimant)))
      case WhatTypeOfAccount.JointNotHeldByClaimant =>
        Some((AccountType.Joint, Some(JointAccountType.NotHeldByClaimant)))
    }
}
