/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.mods.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class AmendmentReference(value: Int)

object AmendmentReference {
  implicit val format: Format[AmendmentReference] = implicitly[Format[Int]].inmap(AmendmentReference(_), _.value)
}
