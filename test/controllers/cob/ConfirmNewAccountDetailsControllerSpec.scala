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

package controllers.cob

import connectors.ChangeOfBankConnector
import controllers.actions.{FakeVerifyBarNotLockedAction, FakeVerifyHICBCAction}
import forms.cob.ConfirmNewAccountDetailsFormProvider
import models.CBEnvelope.CBEnvelope
import models.changeofbank.ClaimantBankInformation
import models.cob.ConfirmNewAccountDetails.Yes
import models.cob.{ConfirmNewAccountDetails, NewAccountDetails, UpdateBankDetailsResponse}
import models.requests.BaseDataRequest
import models.{CBEnvelope, NormalMode, UserAnswers}
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.cob.{ConfirmNewAccountDetailsPage, NewAccountDetailsPage}
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AuditService, ChangeOfBankService}
import testconfig.TestConfig
import testconfig.TestConfig._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseISpec
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.Stubs.userLoggedInChildBenefitUser
import utils.TestData.{NinoUser, claimantBankInformation}
import utils.handlers.ErrorHandler
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ErrorTemplate
import views.html.cob.ConfirmNewAccountDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmNewAccountDetailsControllerSpec extends BaseISpec with MockitoSugar with ScalaCheckPropertyChecks {

  def onwardRoute = Call("GET", "/foo")

  val mockSessionRepository         = mock[SessionRepository]
  val mockCobConnector              = mock[ChangeOfBankConnector]
  val mockErrorHandler              = mock[ErrorHandler]
  implicit val mockExecutionContext = mock[ExecutionContext]
  implicit val mockHeaderCarrier    = mock[HeaderCarrier]
  implicit val mockAuditService     = mock[AuditService]

  lazy val confirmNewAccountDetailsRoute: String =
    controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode).url

  val formProvider = new ConfirmNewAccountDetailsFormProvider()
  val form: Form[ConfirmNewAccountDetails] = formProvider()

  val cobService: ChangeOfBankService = new ChangeOfBankService(mockCobConnector, mockSessionRepository) {
    override def retrieveBankClaimantInfo(implicit
        ec: ExecutionContext,
        hc: HeaderCarrier
    ): CBEnvelope[ClaimantBankInformation] = CBEnvelope(claimantBankInformation)

    override def submitClaimantChangeOfBank(
        newBankAccountInfo: Option[NewAccountDetails],
        request: BaseDataRequest[AnyContent]
    )(implicit
        ec: ExecutionContext,
        hc: HeaderCarrier
    ): CBEnvelope[UpdateBankDetailsResponse] = CBEnvelope(UpdateBankDetailsResponse("submitted"))
  }

  val claimantName      = "John Doe"
  val newAccountDetails = NewAccountDetails("John Doe", "123456", "11110000")

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset[Object](
      mockCobConnector,
      mockSessionRepository,
      mockErrorHandler,
      mockExecutionContext,
      mockHeaderCarrier,
      mockAuditService
    )
  }

  "ConfirmNewAccountDetails Controller" - {

    "when the change of bank feature is enabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

      "must return OK and the correct view for a GET" in {
        userLoggedInChildBenefitUser(NinoUser)

        val userAnswers = UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).toOption
        val application = applicationBuilder(config, userAnswers = userAnswers)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ChangeOfBankService].toInstance(cobService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNewAccountDetailsRoute).withSession("authToken" -> "Bearer 123")
          when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

          val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              form,
              NormalMode,
              claimantName,
              newAccountDetails.newAccountHoldersName,
              newAccountDetails.newSortCode,
              newAccountDetails.newAccountNumber
            )(request, messages(application)).toString
          )
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        userLoggedInChildBenefitUser(NinoUser)

        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
          .set(NewAccountDetailsPage, newAccountDetails)
          .flatMap(ua => ua.set(ConfirmNewAccountDetailsPage, ConfirmNewAccountDetails.values.head))
          .success
          .value

        val application = applicationBuilder(config, userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ChangeOfBankService].toInstance(cobService)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, confirmNewAccountDetailsRoute).withSession("authToken" -> "Bearer 123")
          when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(Some(userAnswers))

          val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              form.fill(ConfirmNewAccountDetails.values.head),
              NormalMode,
              claimantName,
              newAccountDetails.newAccountHoldersName,
              newAccountDetails.newSortCode,
              newAccountDetails.newAccountNumber
            )(request, messages(application)).toString
          )
        }
      }

      "must redirect to the next page when valid data is submitted" in {
        userLoggedInChildBenefitUser(NinoUser)

        val confirmNewAccountDetails = Yes
        val userAnswers = UserAnswers(userAnswersId)
          .set(NewAccountDetailsPage, newAccountDetails)
          .flatMap(_.set(ConfirmNewAccountDetailsPage, confirmNewAccountDetails))
          .toOption

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(config, userAnswers = userAnswers)
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[ChangeOfBankService].toInstance(cobService)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, confirmNewAccountDetailsRoute)
              .withFormUrlEncodedBody(("value", ConfirmNewAccountDetails.values.head.toString))
              .withSession("authToken" -> "Bearer 123")

          when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)
          when(mockSessionRepository.set(userAnswers.get)) thenReturn Future.successful(true)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        userLoggedInChildBenefitUser(NinoUser)

        val mockSessionRepository = mock[SessionRepository]

        val userAnswers = UserAnswers(userAnswersId)
          .set(NewAccountDetailsPage, newAccountDetails)
          .toOption

        val application = applicationBuilder(config, userAnswers = userAnswers)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ChangeOfBankService].toInstance(cobService)
          )
          .build()

        when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

        running(application) {
          val request =
            FakeRequest(POST, confirmNewAccountDetailsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))
              .withSession("authToken" -> "Bearer 123")

          val result    = route(application, request).value
          val boundForm = form.bind(Map("value" -> "invalid value"))
          status(result) mustEqual BAD_REQUEST
          val view = application.injector.instanceOf[ConfirmNewAccountDetailsView]

          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view(
              boundForm,
              NormalMode,
              claimantName,
              newAccountDetails.newAccountHoldersName,
              newAccountDetails.newSortCode,
              newAccountDetails.newAccountNumber
            )(request, messages(application)).toString
          )
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, confirmNewAccountDetailsRoute)
            .withSession("authToken" -> "Bearer 123")
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "redirect to Journey Recovery for a POST if no existing data is found" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, confirmNewAccountDetailsRoute)
              .withFormUrlEncodedBody(("value", ConfirmNewAccountDetails.values.head.toString))
              .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must properly be redirected to Hicbc and Barlock validation in case " in {
        val scenarios = Table(
          ("VerifyHICBC-VerifyBARNotLocked", "StatusAndRedirectUrl"),
          (
            (FakeVerifyHICBCAction(true), FakeVerifyBarNotLockedAction(false)),
            (SEE_OTHER, Some(controllers.cob.routes.BARSLockOutController.onPageLoad().url))
          ),
          (
            (FakeVerifyHICBCAction(false), FakeVerifyBarNotLockedAction(true)),
            (SEE_OTHER, Some(controllers.cob.routes.HICBCOptedOutPaymentsController.onPageLoad().url))
          ),
          ((FakeVerifyHICBCAction(true), FakeVerifyBarNotLockedAction(true)), (OK, None))
        )

        forAll(scenarios) { (actions, statusAndRedirectUrl) =>
          val (hicbcAction, verificationBarAction) = actions
          val (resultStatus, redirectUrl)          = statusAndRedirectUrl

          val userAnswers = UserAnswers(userAnswersId).set(NewAccountDetailsPage, newAccountDetails).toOption

          val application: Application = applicationBuilderWithVerificationActions(
            config,
            userAnswers = userAnswers,
            verifyHICBCAction = hicbcAction,
            verifyBarNotLockedAction = verificationBarAction
          ).overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[ChangeOfBankService].toInstance(cobService)
          ).build()

          running(application) {
            val request = FakeRequest(GET, confirmNewAccountDetailsRoute).withSession("authToken" -> "Bearer 123")
            when(mockSessionRepository.get(userAnswersId)) thenReturn Future.successful(userAnswers)

            val result = route(application, request).value

            status(result) mustEqual resultStatus
            redirectLocation(result) mustEqual (redirectUrl)
          }
        }
      }
    }
    "when the change of bank feature is disabled" - {
      val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

      "must return Not Found and the Error view" in {
        userLoggedInChildBenefitUser(NinoUser)

        val application = applicationBuilder(config, userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, confirmNewAccountDetailsRoute)
            .withSession("authToken" -> "Bearer 123")

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorTemplate]

          status(result) mustEqual NOT_FOUND
          assertSameHtmlAfter(removeCsrfAndNonce)(
            contentAsString(result),
            view("pageNotFound.title", "pageNotFound.heading", "pageNotFound.paragraph1")(
              request,
              messages(application)
            ).toString
          )
        }
      }
    }
  }
}
