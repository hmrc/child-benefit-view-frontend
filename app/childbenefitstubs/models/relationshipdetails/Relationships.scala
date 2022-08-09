/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.relationshipdetails

import play.api.libs.json.{Format, Json}

case class Relationships(relationships: Option[RelationshipDetails])

object Relationships {
  implicit val format: Format[Relationships] = Json.format[Relationships]
}
