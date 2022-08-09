/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import childbenefitstubs.models.failure.{Failure, Failures}
import play.api.http.ContentTypes
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.BadRequest

import scala.concurrent.Future

trait SyntaxExtensions {
  implicit class SingleValidationSyntax[A](val validation: Either[Failure, A]) {
    def ++[B](validation2: Either[Failure, B]): Either[Failures, (A, B)] = {
      (validation, validation2) match {
        case (Left(f1), Left(f2))   => Left(Failures(List(f1, f2)))
        case (Left(f1), _)          => Left(Failures(List(f1)))
        case (_, Left(f2))          => Left(Failures(List(f2)))
        case (Right(a1), Right(a2)) => Right((a1, a2))
      }
    }
  }

  implicit class MultipleValidationsSyntax[A](
      val validation: Either[Failures, A]
  ) {
    def ++[B](validation2: Either[Failure, B]): Either[Failures, (A, B)] = {
      (validation, validation2) match {
        case (Left(f1), Left(f2))   => Left(f1.copy(failures = f1.failures :+ f2))
        case (Left(f1), _)          => Left(f1)
        case (_, Left(f2))          => Left(Failures(List(f2)))
        case (Right(a1), Right(a2)) => Right((a1, a2))
      }
    }
  }

  implicit class ToFutureSyntax[L, R](val either: Either[L, R])(
      implicit
      writes: Writes[L]
  ) {
    def toFuture(action: R => Future[Result]): Future[Result] =
      either match {
        case Left(failures) =>
          Future.successful(
            BadRequest(Json.toJson(failures)).as(ContentTypes.JSON)
          )

        case Right(a) => action(a)
      }
  }
}
