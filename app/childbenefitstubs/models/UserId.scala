/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models

final case class UserId private (value: String) extends ValidatedValue

object UserId extends ValidatedValue.Builder[UserId]("UserId", """^[0-9]{7}$""")
