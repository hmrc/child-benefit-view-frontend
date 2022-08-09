/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models

final case class NINO private (value: String) extends ValidatedValue

object NINO
  extends ValidatedValue.Builder[NINO](
    "NINO",
    """^((?!(BG|GB|KN|NK|NT|TN|ZZ)|([DFIQUV])[A-Z]|[A-Z]([DFIOQUV]))[A-Z]{2})[0-9]{6}[A-D\s]?$"""
  )
