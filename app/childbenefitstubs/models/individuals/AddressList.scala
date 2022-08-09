/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Format, Json}

case class AddressList(address: Option[List[Address]])

object AddressList {
  implicit val format: Format[AddressList] = Json.format[AddressList]
}
