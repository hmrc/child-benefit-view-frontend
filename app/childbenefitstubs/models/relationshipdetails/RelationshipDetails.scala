/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.relationshipdetails

import play.api.libs.json.{Format, Json}

case class RelationshipDetails(relationship: Option[List[Relationship]])

object RelationshipDetails {
  implicit val format: Format[RelationshipDetails] =
    Json.format[RelationshipDetails]
}
