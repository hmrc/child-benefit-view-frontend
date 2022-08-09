/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models

sealed abstract class Environment(val value: String)

object Environment {
  case object Ist0 extends Environment("ist0")
  case object Clone extends Environment("clone")
  case object Live extends Environment("live")

  def from(value: String): Option[Environment] =
    value match {
      case Ist0.value  => Some(Ist0)
      case Clone.value => Some(Clone)
      case Live.value  => Some(Live)
      case _           => None
    }
}
