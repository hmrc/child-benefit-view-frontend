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

package models.entitlement

import models.common.{AddressLine, AddressPostcode}
import play.api.libs.json.Json
import utils.helpers.StringHelper

final case class FullAddress(
    addressLine1:    AddressLine,
    addressLine2:    AddressLine,
    addressLine3:    Option[AddressLine],
    addressLine4:    Option[AddressLine],
    addressLine5:    Option[AddressLine],
    addressPostcode: AddressPostcode
) {
  def toSingleLineString =
    s"${addressLine1.value} " +
      s"${addressLine2.value} " +
      s"${addressLine3.fold("")(_.value)} " +
      s"${addressLine4.fold("")(_.value)} " +
      s"${addressLine5.fold("")(_.value)} " +
      s"${addressPostcode.value}"

  def toPageDisplayString: String = {
    s"${if (!StringHelper.isWhitespaceOnly(addressLine1.value)) addressLine1.value + "</br>" else ""}" +
      s"${if (!StringHelper.isWhitespaceOnly(addressLine2.value)) addressLine2.value + "</br>" else ""}" +
      s"${addressLine3
        .fold("")(address => if (!StringHelper.isWhitespaceOnly(address.value)) address.value + "</br>" else "")} " +
      s"${addressLine4
        .fold("")(address => if (!StringHelper.isWhitespaceOnly(address.value)) address.value + "</br>" else "")} " +
      s"${addressLine5
        .fold("")(address => if (!StringHelper.isWhitespaceOnly(address.value)) address.value + "</br>" else "")} " +
      s"${if (!StringHelper.isWhitespaceOnly(addressPostcode.value)) addressPostcode.value + "</br>" else ""}"
  }
}

object FullAddress {
  implicit val format = Json.format[FullAddress]

}
