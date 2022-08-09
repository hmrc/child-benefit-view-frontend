/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import childbenefitstubs.models.failure.Failure
import childbenefitstubs.models.{Environment, UserId}
import play.api.mvc.{AnyContent, Request}

import java.util.UUID
import scala.util.Try

trait HeaderValidations {
  protected def validateCorrelationId(
      request: Request[AnyContent]
  ): Either[Failure, UUID] =
    validateHeader[UUID](
      request,
      "CorrelationId",
      s => Try(UUID.fromString(s)).toEither
    ).map(_.getOrElse(UUID.randomUUID))

  protected def validateEnvironment(
      request: Request[AnyContent]
  ): Either[Failure, Environment] =
    validateRequiredHeader[Environment](
      request,
      "Environment",
      s => Environment.from(s).toRight("invalid environment"),
      _ => true
    )

  protected def validateOriginatorId(
      request:        Request[AnyContent],
      acceptedValues: Set[String]
  ): Either[Failure, String] =
    validateRequiredHeader[String](
      request,
      "OriginatorId",
      s => Right(s),
      acceptedValues.contains
    )

  protected def validateUserId(
      request:      Request[AnyContent],
      originatorId: String
  ): Either[Failure, Option[UserId]] =
    originatorId match {
      case "DA2_CHB" | "DA2_CHB_DWP" =>
        validateRequiredHeader[UserId](
          request,
          "UserId",
          UserId.from,
          _ => true
        ).map(Option.apply)

      case _ => Right(None)
    }

  protected def validateAudience(
      request:        Request[AnyContent],
      originatorId:   String,
      acceptedValues: Set[String]
  ): Either[Failure, Option[String]] =
    originatorId match {
      case "DA2_CHB_DWP" =>
        validateRequiredHeader[String](
          request,
          "Audience",
          Right.apply,
          acceptedValues.contains
        ).map(Option.apply)

      case _ => Right(None)
    }

  protected def validateAlignment(
      request:        Request[AnyContent],
      originatorId:   String,
      acceptedValues: Set[String]
  ): Either[Failure, Option[String]] =
    originatorId match {
      case "DA2_CHB_DWP" =>
        validateRequiredHeader[String](
          request,
          "Alignment",
          Right.apply,
          acceptedValues.contains
        ).map(Option.apply)

      case _ => Right(None)
    }

  private def validateHeader[A](
      request: Request[AnyContent],
      name:    String,
      build:   String => Either[_, A]
  ): Either[Failure, Option[A]] =
    request.headers.get(name) match {
      case None => Right(None)

      case Some(s) =>
        build(s).toOption.toRight(fail("Invalid", name)).map(Option.apply)
    }

  private def validateRequiredHeader[A](
      request:    Request[AnyContent],
      name:       String,
      build:      String => Either[_, A],
      validation: A => Boolean
  ): Either[Failure, A] =
    validateHeader[A](request, name, build) match {
      case Left(e)                          => Left(e)
      case Right(None)                      => Left(fail("Missing", name))
      case Right(Some(a)) if !validation(a) => Left(fail("Invalid", name))
      case Right(Some(a))                   => Right(a)
    }

  private def fail(how: String, name: String): Failure =
    Failure(
      s"${how.toUpperCase}_${name.toUpperCase}",
      s"Submission has not passed validation. $how header $name."
    )
}
