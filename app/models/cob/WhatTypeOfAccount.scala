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

  case object SoleAccount                   extends WithName("sole") with WhatTypeOfAccount
  case object JointAccountSharedWithSomeone extends WithName("joint_shared") with WhatTypeOfAccount
  case object JointAccountNotHeldByYou      extends WithName("joint_not_shared") with WhatTypeOfAccount

  val values: Seq[WhatTypeOfAccount] = Seq(
    SoleAccount,
    JointAccountSharedWithSomeone,
    JointAccountNotHeldByYou
  )

  implicit val enumerable: Enumerable[WhatTypeOfAccount] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
