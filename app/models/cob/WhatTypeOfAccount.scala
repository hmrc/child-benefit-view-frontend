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

import models.{Enumerable, WithName}

sealed trait WhatTypeOfAccount

object WhatTypeOfAccount extends Enumerable.Implicits {

  case object Sole                   extends WithName("sole") with WhatTypeOfAccount
  case object JointHeldByClaimant    extends WithName("joint_held_by_claimant") with WhatTypeOfAccount
  case object JointNotHeldByClaimant extends WithName("joint_not_held_by_claimant") with WhatTypeOfAccount

  val values = List(Sole, JointHeldByClaimant, JointNotHeldByClaimant)

  implicit val enumerable: Enumerable[WhatTypeOfAccount] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

sealed trait AccountType

object AccountType extends Enumerable.Implicits {

  val name = "account_type"

  case object Sole  extends WithName("sole") with AccountType
  case object Joint extends WithName("joint") with AccountType

  implicit val enumerable: Enumerable[AccountType] =
    Enumerable(List(Sole, Joint).map(v => v.toString -> v): _*)
}

sealed trait JointAccountType

object JointAccountType extends Enumerable.Implicits {

  val name = "joint_account_type"

  case object HeldByClaimant    extends WithName("held_by_claimant") with JointAccountType
  case object NotHeldByClaimant extends WithName("not_held_by_claimant") with JointAccountType

  implicit val enumerable: Enumerable[JointAccountType] =
    Enumerable(List(HeldByClaimant, NotHeldByClaimant).map(v => v.toString -> v): _*)
}
