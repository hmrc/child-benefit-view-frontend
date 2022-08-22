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

package controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.ProofOfEntitlementService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ProofOfEntitlement

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ProofOfEntitlementController @Inject()(
  mcc: MessagesControllerComponents,
  proofOfEntitlement: ProofOfEntitlement,
  proofOfEntitlementService : ProofOfEntitlementService
) extends FrontendController(mcc) {


  val view: Action[AnyContent] =
    Action.async { implicit request =>
      proofOfEntitlementService.getEntitlement().map { entitlement =>
        Ok(proofOfEntitlement(entitlement))
      }
    }
}

object ProofOfEntitlementController {
  def formatDate(date: LocalDate)(implicit messages: Messages): String =
    date.format(
      DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.locale)
    )

  def formatMoney(amount: Double, currency: String = "£"): String =
    f"$currency$amount%.2f"

  def multiLineTextAsHtml(text: String): Html =
    Html(text.replaceAll("\\n", "<br>"))
}
