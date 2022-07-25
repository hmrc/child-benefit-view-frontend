/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt.model

class ExtendedString(s: String) {
  def isNumber: Boolean = s.matches("[-+]?\\d+(\\.\\d+)?")
}

object ExtendedString {
  implicit def String2ExtendedString(s: String): ExtendedString = new ExtendedString(s)
}
