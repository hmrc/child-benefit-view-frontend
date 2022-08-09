/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models

import play.api.libs.json.{Format, JsString, Reads, Writes}

trait ValidatedValue {
  val value: String

  override def toString: String = value
}

object ValidatedValue {
  abstract class Builder[A <: ValidatedValue](val name:  String,
                                              val regex: String)
    extends (String => A) {
    def from(value: String): Either[String, A] =
      Either.cond(
        value.matches(regex),
        apply(value),
        s"$value is not a valid $name"
      )

    implicit val format: Format[A] =
      Format(
        implicitly[Reads[String]]
          .flatMap(s => from(s).fold(e => Reads.failed(e), a => Reads.pure(a))),
        Writes.apply(n => JsString(n.value))
      )
  }
}
