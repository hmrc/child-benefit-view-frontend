/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs

import ch.qos.logback.core.util.FileUtil

import java.time.LocalDate.now
import java.time.format.DateTimeFormatter.ISO_DATE
import scala.collection.mutable
import play.api.mvc._
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import util.FileUtils

@Singleton()
class DESDataStubController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {

  def paymentDetails(identifier: String): Action[AnyContent] = Action { implicit request =>

    val content = FileUtils.readContent("des/api/child-benefit-financial-details", identifier)

    Ok(content).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")

  }

  def relationship(idNumber: String): Action[AnyContent] = Action { implicit request =>

    val content = FileUtils.readContent("des/api/individuals-relationship-details", idNumber)

    Ok(content).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")

  }

  def individualsDetails(idNumber: String, resolveMerge: String): Action[AnyContent] = Action { implicit request =>

    //todo try to use  resolveMerge if needed to provide defferent calls

    resolveMerge match {
      case "N" | "n" => {
        val content = FileUtils.readContent(s"des/api/individual-details/N", idNumber)

        Ok(content).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")
      }
      case "Y" | "y" => {
        val content = FileUtils.readContent(s"des/api/individual-details/Y", idNumber)
        Ok(content).withHeaders("CorrelationId" -> "a1e8057e-fbbc-47a8-a8b4-78d9f015c253", "Content-Type" -> "application/json")
      }

    }

  }

}
