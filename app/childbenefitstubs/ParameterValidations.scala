/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import childbenefitstubs.models.failure.Failure
import childbenefitstubs.models.{NINO, TRN}

trait ParameterValidations {
  protected def validateNINO(identifier: String): Either[Failure, NINO] =
    NINO.from(identifier).left.map(_ => fail("identifier"))

  protected def validateNINOOrTRN(
      identifier: String
  ): Either[Failure, Either[NINO, TRN]] =
    validateNINO(identifier) match {
      case Right(nino) => Right(Left(nino))

      case Left(_) =>
        TRN.from(identifier) match {
          case Right(trn) => Right(Right(trn))
          case Left(_)    => Left(fail("idNumber"))
        }
    }

  protected def validateYesOrNo(resolveMerge:  String,
                                value: String): Either[Failure, Boolean] =
    value match {
      case "Y" => Right(true)
      case "N" => Right(false)
      case _   => Left(fail(resolveMerge))
    }

  private def fail(name: String): Failure =
    Failure(
      s"INVALID_${name.toUpperCase}",
      s"Submission has not passed validation. Invalid parameter $name."
    )
}
