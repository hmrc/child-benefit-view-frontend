/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.failure

import play.api.libs.json.{Format, Json}

case class Failures(failures: List[Failure])

object Failures {
  implicit val format: Format[Failures] = Json.format[Failures]
}
