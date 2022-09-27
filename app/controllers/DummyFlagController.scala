package controllers

import controllers.actions.FeatureFlagSupport
import features.{FeatureFlag, FeatureFlagService}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DummyFlagView

import javax.inject.Inject
import scala.concurrent.Future

class DummyFlagController @Inject() (
    val controllerComponents:  MessagesControllerComponents,
    view:                      DummyFlagView,
    override val featureFlags: FeatureFlagService
) extends FrontendBaseController
    with I18nSupport
    with FeatureFlagSupport {
  def onPageLoad: Action[AnyContent] =
    whenEnabled(FeatureFlag.DummyFlag) { implicit request =>
      Future.successful(Ok(view()))
    }
}
