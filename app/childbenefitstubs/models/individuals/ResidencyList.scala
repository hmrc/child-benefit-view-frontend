/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.json.{Json, OFormat}

case class ResidencyList(residency: Option[List[Residency]])

object ResidencyList {
  implicit val format: OFormat[ResidencyList] = Json.format[ResidencyList]
}
