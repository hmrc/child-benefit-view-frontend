/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.failure

import play.api.libs.json.{Format, Json}

case class Failure(code: String, reason: String)

object Failure {
  implicit val format: Format[Failure] = Json.format[Failure]
}
