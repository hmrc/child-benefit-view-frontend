/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Json, OFormat}

case class NameList(name: Option[List[Name]])

object NameList {
  implicit val format: OFormat[NameList] = Json.format[NameList]
}
