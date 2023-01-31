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

package models.ftnae

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhichYoungPerson

object WhichYoungPerson extends Enumerable.Implicits {

  case object Child1         extends WithName("child1") with WhichYoungPerson
  case object Child2         extends WithName("child2") with WhichYoungPerson
  case object ChildNotListed extends WithName("childNotListed") with WhichYoungPerson

  val values: Seq[WhichYoungPerson] = Seq(
    Child1,
    Child2,
    ChildNotListed
  )

  def options(implicit messages: Messages): Seq[RadioItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"whichYoungPerson.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }

  implicit val enumerable: Enumerable[WhichYoungPerson] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
