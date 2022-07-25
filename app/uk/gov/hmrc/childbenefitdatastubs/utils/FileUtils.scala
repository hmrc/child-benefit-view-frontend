/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.childbenefitdatastubs.utils

import play.api.mvc.Results._
import play.api.http.MimeTypes
import play.api.mvc._

object FileUtils  {

  private[utils] def dataAsResponse(data: Option[String]): Result = data.map {
    case "" => NoContent
    case content if content.contains("\"statusCode\": 400") => BadRequest(content).as(MimeTypes.JSON)
    case content if content.contains("\"statusCode\": 403") => Forbidden(content).as(MimeTypes.JSON)
    case content if content.contains("\"statusCode\": 500") => InternalServerError(content).as(MimeTypes.JSON)
    case content => Ok(content).as(MimeTypes.JSON)
  }.getOrElse(NotFound("{\"statusCode\": 404, \"message\": \"Not Found\"}").as(MimeTypes.JSON))
}