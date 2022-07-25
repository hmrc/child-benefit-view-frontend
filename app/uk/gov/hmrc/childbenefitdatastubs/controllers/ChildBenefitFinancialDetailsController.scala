/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.childbenefitdatastubs.controllers

import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class ChildBenefitFinancialDetailsController @Inject()(cc: ControllerComponents)
    extends BackendController(cc) {

  def hello(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok("Hello world"))
  }

  /*
    Example URIs
    /child-benefit/financial-details/AB654321
    /child-benefit/financial-details/YY123499D
   */
  def financialDetails(correlationId:String ): Action[AnyContent] = Action.async { implicit r =>
    Future.successful(Ok("Hello world"))
  }

}
