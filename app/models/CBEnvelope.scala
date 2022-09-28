/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import models.errors.CBError
import cats.data.EitherT
import cats.syntax.either._
import scala.concurrent.{ExecutionContext, Future}

object CBEnvelope {
  type CBEnvelope[T] = EitherT[Future, CBError, T]

  def apply[T](value: T): CBEnvelope[T] =
    EitherT[Future, CBError, T](Future.successful(value.asRight[CBError]))

  def apply[T, E](value: Either[CBError, T]): CBEnvelope[T] =
    EitherT(Future successful value)

  def fromEitherF[E <: CBError, T](value: Future[Either[E, T]]): CBEnvelope[T] = EitherT(value)

  def fromError[E <: CBError, T](error: E): CBEnvelope[T] = EitherT(Future.successful(error.asLeft[T]))

  def fromF[T](value: Future[T])(implicit ec: ExecutionContext): CBEnvelope[T] = EitherT(value.map(_.asRight[CBError]))
}
