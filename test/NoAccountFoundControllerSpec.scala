import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.AuthStub.userLoggedInChildBenefitUser
import utils.BaseISpec
import utils.NonceUtils.removeNonce
import utils.TestData.NinoUser
import views.html.NoAccountFoundView

class NoAccountFoundControllerSpec extends BaseISpec {
  "NoAccountFoundController" - {
    "must return OK and the correct view for a GET" in {
      userLoggedInChildBenefitUser(NinoUser)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoAccountFoundController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoAccountFoundView]

        status(result) mustEqual OK
        assertSameHtmlAfter(removeNonce)(contentAsString(result), view()(request, messages(application)).toString)

      }
    }

    "must return UNAUTHORISED and the correct view for a GET" in {
      afterReset {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.NoAccountFoundController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value must startWith(application.configuration.get[String]("urls.login"))

        }
      }
    }
  }
}
