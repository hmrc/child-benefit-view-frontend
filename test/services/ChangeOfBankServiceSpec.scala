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

package services

import base.BaseSpec
import cats.implicits.catsSyntaxEitherId
import connectors.ChangeOfBankConnector
import controllers.{cob, routes}
import models.changeofbank._
import models.cob.{NewAccountDetails, UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.common.{AdjustmentReasonCode, NationalInsuranceNumber}
import models.errors.{CBError, ChangeOfBankValidationError, ConnectorError}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{CBEnvelope, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status.{NOT_FOUND, NOT_IMPLEMENTED}
import play.api.i18n.Messages
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.helpers.ClaimantBankInformationHelper.formatClaimantBankInformation
import views.html.cob.ChangeAccountView

import scala.concurrent.{ExecutionContext, Future}

class ChangeOfBankServiceSpec extends BaseSpec {
  val cobConnector: ChangeOfBankConnector  = mock[ChangeOfBankConnector]
  val sessionRepository: SessionRepository = mock[SessionRepository]
  implicit val auditService: AuditService  = mock[AuditService]
  implicit val ec: ExecutionContext        = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier           = HeaderCarrier()

  val sut = new ChangeOfBankService(cobConnector, sessionRepository)

  "ChangeOfBankService" - {
    "retrieveBankClaimantInfo" - {
      "GIVEN that ChangeOfBankConnector.getChangeOfBankClaimantInfo successfully returns a ClaimantBankInformation" - {
        "THEN the same ClaimantBankInformation is successfully returned formatted" in {
          forAll(arbitrary[ClaimantBankInformation]) { claimantBankInformation =>
            when(cobConnector.getChangeOfBankClaimantInfo).thenReturn(CBEnvelope(claimantBankInformation))

            whenReady(sut.retrieveBankClaimantInfo.value) { result =>
              result mustBe Right(formatClaimantBankInformation(claimantBankInformation))
            }
          }
        }
      }

      "GIVEN that ChangeOfBankConnector.getChangeOfBankClaimantInfo fails and returns a CBError" - {
        "THEN the same CBError is returned" in {
          val expectedError = ConnectorError(NOT_IMPLEMENTED, "Unit Test connector error")
          when(cobConnector.getChangeOfBankClaimantInfo).thenReturn(CBEnvelope.fromError(expectedError))

          whenReady(sut.retrieveBankClaimantInfo.value) { result =>
            result mustBe Left(expectedError)
          }
        }
      }
    }

    "validate" - {
      "GIVEN a valid AccountHolderName, SortCode and BankAccountNumber" - {
        "AND that ChangeOfBankConnector.verifyClaimantBankAccount successfully returns without an Error" - {
          "THEN Unit is returned as expected" in {
            forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber]) {
              (accountHoldersName, sortCode, bankAccountNumber) =>
                when(
                  cobConnector.verifyClaimantBankAccount(
                    VerifyBankAccountRequest(accountHoldersName, sortCode, bankAccountNumber)
                  )
                )
                  .thenReturn(CBEnvelope(()))

                whenReady(sut.validate(accountHoldersName, sortCode, bankAccountNumber).value) { result =>
                  result mustBe Right(())
                }
            }
          }
        }
      }

      "GIVEN that ChangeOfBankConnector.verifyClaimantBankAccount fails and returns a CBError" - {
        "THEN the same CBError is returned" in {
          forAll(arbitrary[AccountHolderName], arbitrary[SortCode], arbitrary[BankAccountNumber]) {
            (accountHoldersName, sortCode, bankAccountNumber) =>
              val expectedError = ConnectorError(NOT_IMPLEMENTED, "Unit Test connector error")
              when(
                cobConnector
                  .verifyClaimantBankAccount(any[VerifyBankAccountRequest])(any[ExecutionContext], any[HeaderCarrier])
              )
                .thenReturn(CBEnvelope.fromError(expectedError))

              whenReady(sut.validate(accountHoldersName, sortCode, bankAccountNumber).value) { result =>
                result mustBe Left(expectedError)
              }
          }
        }
      }
    }

    "processClaimantInformation" - {
      val view = mock[ChangeAccountView]
      when(view(any[String], any[ClaimantBankAccountInformation])(any[Request[_]], any[Messages]))
        .thenReturn(Html("Unit Test content"))
      implicit val mockRequest  = mock[OptionalDataRequest[_]]
      implicit val mockMessages = mock[Messages]

      "GIVEN that BankClaimantDetails are successfully retrieved" - {
        val hicbcClaimantTransformer: ClaimantBankInformation => ClaimantBankInformation =
          claimant =>
            claimant.copy(financialDetails =
              claimant.financialDetails.copy(
                adjustmentReasonCode = Some(AdjustmentReasonCode(ChangeOfBankService.HICBCAdjustmentCode)),
                adjustmentEndDate = Some(ChangeOfBankService.today.plusDays(1))
              )
            )
        val awardEndsInTheFutureNoRollNumber: ClaimantBankInformation => ClaimantBankInformation =
          claimant =>
            claimant.copy(financialDetails =
              claimant.financialDetails.copy(
                awardEndDate = ChangeOfBankService.today.plusDays(1),
                bankAccountInformation = claimant.financialDetails.bankAccountInformation.copy(
                  buildingSocietyRollNumber = None
                )
              )
            )
        val awardEndsInTheFutureWithRollNumber: ClaimantBankInformation => ClaimantBankInformation =
          claimant =>
            claimant.copy(financialDetails =
              claimant.financialDetails.copy(
                awardEndDate = ChangeOfBankService.today.plusDays(1),
                bankAccountInformation = claimant.financialDetails.bankAccountInformation.copy(
                  buildingSocietyRollNumber = Some(BuildingSocietyRollNumber("Definite"))
                )
              )
            )
        val awardEndsInThePast: ClaimantBankInformation => ClaimantBankInformation =
          claimant =>
            claimant.copy(financialDetails =
              claimant.financialDetails.copy(
                awardEndDate = ChangeOfBankService.today.minusDays(1)
              )
            )

        forAll(
          Table(
            ("claimantState", "claimantTransformer", "expectedResultName", "expectedResult"),
            (
              "the claimant is HICBC with an adjustment end date in the future but no roll number",
              hicbcClaimantTransformer,
              "a Redirect to the HICBC Opted Out Payments page",
              (_: ClaimantBankAccountInformation) => Redirect(cob.routes.HICBCOptedOutPaymentsController.onPageLoad())
            ),
            (
              "the claimant has an award end date in the future but no building society roll number",
              awardEndsInTheFutureNoRollNumber,
              "an OK to the expected Change Account View",
              (cBAI: ClaimantBankAccountInformation) => Ok(view("", cBAI))
            ),
            (
              "the claimant has an award end date in the future but has a building society roll number",
              awardEndsInTheFutureWithRollNumber,
              "an OK to the expected Change Account View",
              (cBAI: ClaimantBankAccountInformation) => Ok(view("", cBAI))
            ),
            (
              "the claimant has an award date that is on the past",
              awardEndsInThePast,
              "a Redirect to the No Account Found page",
              (_: ClaimantBankAccountInformation) => Redirect(routes.NoAccountFoundController.onPageLoad)
            )
          )
        ) {
          (
              claimantState:       String,
              claimantTransformer: ClaimantBankInformation => ClaimantBankInformation,
              expectedResultName:  String,
              expectedResult:      ClaimantBankAccountInformation => Result
          ) =>
            s"AND $claimantState" - {
              s"THEN the returned result is $expectedResultName" in {
                forAll(arbitrary[ClaimantBankInformation]) { claimantBankInformation =>
                  val transformedInfo = claimantTransformer(claimantBankInformation)
                  when(cobConnector.getChangeOfBankClaimantInfo).thenReturn(CBEnvelope(transformedInfo))

                  whenReady(sut.processClaimantInformation(view).value) { result =>
                    result.fold(
                      e => {
                        e mustBe ChangeOfBankValidationError(NOT_FOUND)
                      },
                      r => {
                        r mustBe expectedResult(claimantBankInformation.financialDetails.bankAccountInformation)
                      }
                    )
                  }
                }
              }
            }
        }

        "GIVEN that ChangeOfBankConnector.verifyClaimantBankAccount fails and returns a CBError" - {
          "THEN the same CBError is returned" in {
            val expectedError = ConnectorError(NOT_IMPLEMENTED, "Unit Test connector error")
            when(cobConnector.getChangeOfBankClaimantInfo).thenReturn(CBEnvelope.fromError(expectedError))

            whenReady(sut.processClaimantInformation(view).value) { result =>
              result mustBe Left(expectedError)
            }
          }
        }
      }
    }

    "submitClaimantChangeOfBank" - {
      "GIVEN a valid NewAccountDetails, Id and UserAnswers" - {
        "AND that ChangeOfBankConnector.updateBankAccount successfully returns without a CBError" - {
          "THEN the expected UpdateBankDetailsResponse is returned" - {
            "AND the SessionRepository is cleared for the respective user" in {
              val expectedResponse = UpdateBankDetailsResponse("UNIT_TEST_STATUS")
              when(
                cobConnector.updateBankAccount(any[UpdateBankAccountRequest])(any[ExecutionContext], any[HeaderCarrier])
              )
                .thenReturn(CBEnvelope(expectedResponse))
              when(sessionRepository.clear(any[String]))
                .thenReturn(Future.successful(true))

              forAll(arbitrary[NewAccountDetails], generateId) { (accountDetails, id) =>
                val request: DataRequest[AnyContent] =
                  DataRequest(FakeRequest(), id, NationalInsuranceNumber(id), UserAnswers(id))

                whenReady(sut.submitClaimantChangeOfBank(Some(accountDetails), request).value) { response =>
                  response mustBe expectedResponse.asRight[CBError]
                  verify(sessionRepository).clear(id)
                }
              }
            }
          }
        }
      }
    }
  }
}
