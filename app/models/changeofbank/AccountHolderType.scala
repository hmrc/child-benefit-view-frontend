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

package models.changeofbank

import models.Enumerable
import play.api.libs.json.{JsonValidationError, Reads, Writes}

sealed trait AccountHolderType

object AccountHolderType extends Enumerable.Implicits {
  case object Claimant    extends AccountHolderType
  case object Joint       extends AccountHolderType
  case object SomeoneElse extends AccountHolderType

  val values: List[AccountHolderType] = List(Claimant, Joint, SomeoneElse)

  implicit val enumerable: Enumerable[AccountHolderType] =
    Enumerable(values.map(v => v.toString -> v)*)

  implicit val reads: Reads[AccountHolderType] =
    implicitly[Reads[String]]
      .collect[AccountHolderType](JsonValidationError("Invalid DataFormat")) {
        case "CLAIMANT"     => Claimant
        case "JOINT"        => Joint
        case "SOMEONE_ELSE" => SomeoneElse
      }

  implicit val writes: Writes[AccountHolderType] = implicitly[Writes[String]].contramap[AccountHolderType] {
    case Claimant    => "CLAIMANT"
    case Joint       => "JOINT"
    case SomeoneElse => "SOMEONE_ELSE"
  }
}
