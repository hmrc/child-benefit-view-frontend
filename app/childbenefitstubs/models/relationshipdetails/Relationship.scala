/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.relationshipdetails

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Relationship(nino:                  String,
                        associatedNino:        String,
                        relationshipType:      String,
                        relationshipStartDate: LocalDate,
                        relationshipEndDate:   LocalDate,
                        relationshipSource:    String,
                        relationshipSeqNo:     Int,
                        optimisticLock:        Int)

object Relationship {
  implicit val format: OFormat[Relationship] = Json.format[Relationship]
}
