/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models

final case class TRN private (value: String) extends ValidatedValue

object TRN
  extends ValidatedValue.Builder[TRN]("TRN", """^[0-9]{2}[A-Z]{1}[0-9]{5}$""")
