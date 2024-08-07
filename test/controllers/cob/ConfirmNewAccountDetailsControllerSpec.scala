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

import base.BaseAppSpec
import connectors.ChangeOfBankConnector
import models.CBEnvelope.CBEnvelope
import models.changeofbank.ClaimantBankInformation
import models.cob.{NewAccountDetails, UpdateBankDetailsResponse, WhatTypeOfAccount}
import models.pertaxAuth.PertaxAuthResponseModel
import models.requests.BaseDataRequest
import models.viewmodels.govuk.summarylist._
import models.{CBEnvelope, NormalMode, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.cob.{NewAccountDetailsPage, WhatTypeOfAccountPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import services.{AuditService, ChangeOfBankService}
import stubs.AuthStubs._
import testconfig.TestConfig
import testconfig.TestConfig._
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList}
import uk.gov.hmrc.http.HeaderCarrier
import utils.HtmlMatcherUtils.removeCsrfAndNonce
import utils.TestData.{ninoUser, testClaimantBankInformation}
import utils.handlers.ErrorHandler
import utils.navigation.{FakeNavigator, Navigator}
import views.html.ErrorTemplate
import views.html.cob.ConfirmNewAccountDetailsView

import scala.concurrent.{ExecutionContext, Future}

class ConfirmNewAccountDetailsControllerSpec extends BaseAppSpec with MockitoSugar with ScalaCheckPropertyChecks {

  def onwardRoute: Call = Call("GET", "/foo")

  val mockSessionRepository:         SessionRepository     = mock[SessionRepository]
  val mockCobConnector:              ChangeOfBankConnector = mock[ChangeOfBankConnector]
  val mockErrorHandler:              ErrorHandler          = mock[ErrorHandler]
  implicit val mockExecutionContext: ExecutionContext      = mock[ExecutionContext]
  implicit val mockHeaderCarrier:    HeaderCarrier         = mock[HeaderCarrier]
  implicit val mockAuditService:     AuditService          = mock[AuditService]
  implicit val mockMessages:         Messages              = mock[Messages]
  when(mockMessages("whatTypeOfAccount.options.sole")).thenReturn("Sole account")

  lazy val confirmNewAccountDetailsRoute: String =
    controllers.cob.routes.ConfirmNewAccountDetailsController.onPageLoad(NormalMode).url

  val cobService: ChangeOfBankService = new ChangeOfBankService(mockCobConnector, mockSessionRepository) {
    override def retrieveBankClaimantInfo(implicit
        ec: ExecutionContext,
        hc: HeaderCarrier
    ): CBEnvelope[ClaimantBankInformation] = CBEnvelope(testClaimantBankInformation)

    override def submitClaimantChangeOfBank(
        newBankAccountInfo: Option[NewAccountDetails],
        request:            BaseDataRequest[AnyContent]
    )(implicit
        ec: ExecutionContext,
        hc: HeaderCarrier
    ): CBEnvelope[UpdateBankDetailsResponse] = CBEnvelope(UpdateBankDetailsResponse("submitted"))
  }

  val typeOfAccount:     WhatTypeOfAccount = WhatTypeOfAccount.Sole
  val newAccountDetails: NewAccountDetails = NewAccountDetails("John Doe", "123456", "11110000")
  def summaryList(messages: Messages): SummaryList =
    SummaryListViewModel(
      Seq(
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountType.label"))),
          value = ValueViewModel(HtmlContent(typeOfAccount.message())),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-account-type")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountType.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountHoldersName.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(newAccountDetails.newAccountHoldersName))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountHoldersName.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.sortCode.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(newAccountDetails.newSortCode))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.sortCode.change.hidden"))
          )
        ),
        SummaryListRowViewModel(
          key = Key(content = Text(messages("confirmNewAccountDetails.summary.accountNumber.label"))),
          value = ValueViewModel(HtmlContent(HtmlFormat.escape(newAccountDetails.newAccountNumber))),
          actions = Seq(
            ActionItemViewModel(Text(messages("site.change")), "/child-benefit/change-bank/change-new-account-details")
              .withVisuallyHiddenText(messages("confirmNewAccountDetails.summary.accountNumber.change.hidden"))
          )
        )
      )
    )
      .withCssClass("govuk-!-margin-bottom-9")
      .withAttribute("id" -> "account-details-list")

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
    "onPageLoad" - {
      "GIVEN the change of bank feature is enabled" - {
        val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = true))

        "WHEN valid User Answers are retrieved" - {
          "THEN should return OK Result and the expected view" in {
            mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
            userLoggedInIsChildBenefitUser(ninoUser)

            val userAnswers = UserAnswers(userAnswersId)
              .set(WhatTypeOfAccountPage, typeOfAccount)
              .get
              .set(NewAccountDetailsPage, newAccountDetails)
              .toOption
            val application = applicationBuilderWithVerificationActions(config, userAnswers = userAnswers)
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
                  NormalMode,
                  summaryList(messages(application))
                )(request, messages(application)).toString
              )
            }
          }
        }
      }

      "GIVEN the change of bank feature is disabled" - {
        val config = TestConfig().withFeatureFlags(featureFlags(changeOfBank = false))

        "WHEN a call is made" - {
          "THEN should return Not Found result and the Error View" in {
            mockPostPertaxAuth(PertaxAuthResponseModel("ACCESS_GRANTED", "A field", None, None))
            userLoggedInIsChildBenefitUser(ninoUser)

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
  }
}
