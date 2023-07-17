/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import cats.Show
import cats.syntax.all._
import models.CBEnvelope.CBEnvelope
import models.errors.{CBError, ConnectorError}
import play.api.http.Status
import play.api.libs.json.{JsPath, JsonValidationError, Reads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import utils.logging.RequestLogger

import scala.util.{Failure, Success, Try}

trait HttpReadsWrapper[E] {

  private implicit val logger = new RequestLogger(this.getClass)
  def withHttpReads[T](
      block: HttpReads[Either[CBError, T]] => CBEnvelope[T]
  )(implicit
      readsSuccess: Reads[T],
      readsError:   Reads[E],
      hc:           HeaderCarrier
  ): CBEnvelope[T] =
    block(getHttpReads(readsSuccess, readsError, hc))

  private def getHttpReads[T](implicit
      readsSuccess: Reads[T],
      readsError:   Reads[E],
      hc:           HeaderCarrier
  ): HttpReads[Either[CBError, T]] =
    (_, _, response) => {
      response.status match {
        case Status.OK | Status.CREATED =>
          Try(response.json) match {
            case Success(value) =>
              value
                .validate[T]
                .fold(
                  error => {
                    val errorMessage = s"Couldn't parse body from upstream: error=${error.mkString(", ")}"
                    logger.error(errorMessage)
                    ConnectorError(Status.SERVICE_UNAVAILABLE, errorMessage).asLeft[T]
                  },
                  Right(_)
                )
            case Failure(e) =>
              val errorMessage = s"Couldn't parse error body from upstream, error=${e.getMessage}"
              logger.error(errorMessage)
              ConnectorError(Status.INTERNAL_SERVER_ERROR, errorMessage).asLeft[T]
          }

        case status =>
          Try(response.json) match {
            case Success(value) =>
              value
                .validate[E]
                .fold(
                  e => {
                    val errorMessage = s"Couldn't parse error body from upstream, error=${e.mkString(", ")}"
                    ConnectorError(Status.SERVICE_UNAVAILABLE, errorMessage)
                  },
                  error => fromUpstreamErrorToCBError(status, error)
                )
                .asLeft[T]
            case Failure(e) =>
              val errorMessage = s"Couldn't parse error body from upstream, error=${e.getMessage}"
              logger.error(errorMessage)
              ConnectorError(status, errorMessage).asLeft[T]
          }
      }
    }

  def fromUpstreamErrorToCBError(status: Int, upstreamError: E): CBError
}

object HttpReadsWrapper {
  type ParseErrors = Seq[(JsPath, Seq[JsonValidationError])]
  implicit val showPath: Show[ParseErrors] = Show.show { errors =>
    errors
      .map { error =>
        val (e, seq) = error
        s"[$e: ${seq.mkString(", ")}]"
      }
      .mkString(", ")
  }
}
