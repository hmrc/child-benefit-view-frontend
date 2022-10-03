package controllers

import controllers.featureswitch.DummyFeatureSwitchController
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import utils.BaseISpec
import play.api.test.Helpers._

class DummyFeatureSwitchControllerSpec extends BaseISpec {

  private val fakeRequest = FakeRequest("GET", "/")

  "Feature switch" - {
    "prevent controller to move on and return NOT_FOUND when off" - {
      val app        = applicationBuilder().configure(conf = Map("feature-switch" -> false)).build()
      val controller = app.injector.instanceOf[DummyFeatureSwitchController]
      val result     = controller.onPageLoad(fakeRequest)
      status(result) mustEqual Status.NOT_FOUND
    }

    "allow controller to move on when on" in {
      val app        = applicationBuilder().configure(conf = Map("feature-switch" -> true)).build()
      val controller = app.injector.instanceOf[DummyFeatureSwitchController]
      val result     = controller.onPageLoad(fakeRequest)
      status(result) mustEqual Status.OK
    }
  }
}
