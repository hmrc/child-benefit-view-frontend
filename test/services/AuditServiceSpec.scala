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
import com.google.common.io.BaseEncoding
import models.audit._
import models.common.{ChildReferenceNumber, FirstForename, NationalInsuranceNumber, Surname}
import models.entitlement.Child
import models.ftnae.{CourseDuration, FtnaeChildInfo, FtnaeQuestionAndAnswer}
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.i18n.Messages
import play.api.libs.json.Writes
import play.api.mvc.{Cookie, Headers, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.TestData.{testClaimantBankInformation, testEntitlement}
import utils.helpers.ClaimantBankInformationHelper

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class AuditServiceSpec extends BaseSpec {

  val auditConnector: AuditConnector = mock[AuditConnector]
  val sut:            AuditService   = new AuditService(auditConnector)

  val testNino:             String = "CA123456A"
  val testCRN:              String = "AC654321C"
  val testStatus:           String = "testStatus"
  val testReferrerValue:    String = "/foo"
  val testFingerprintValue: String = "testDeviceFingerprint"

  protected val request: Request[_] =
    FakeRequest()
      .withHeaders(Headers(("referer", testReferrerValue)))
      .withCookies(Cookie("mdtpdf", BaseEncoding.base64().encode(testFingerprintValue.toCharArray.map(c => c.toByte))))
  protected val optionalDataRequest: OptionalDataRequest[_] =
    OptionalDataRequest(request, "123", NationalInsuranceNumber(testNino), None)

  protected implicit val ec:       ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc:       HeaderCarrier    = HeaderCarrier()
  protected implicit val messages: Messages         = mock[Messages]

  def testForCapturedValue[T](beingChecked: String, fieldName: String, fieldValue: T, expectedResult: T): Unit = {
    s"AND the $beingChecked is sent with the provided $fieldName" in {
      fieldValue mustBe expectedResult
    }
  }

  "auditProofOfEntitlement" - {
    "GIVEN a ChildBenefitEntitlement" - {
      "WHEN an audit call is made for auditProofOfEntitlement" - {
        val captor: ArgumentCaptor[ViewProofOfEntitlementModel] =
          ArgumentCaptor.forClass(classOf[ViewProofOfEntitlementModel])

        forAll(Table("withEntitlement", true, false)) { withEntitlement =>
          s"AND an entitlement ${isOrIsNot(withEntitlement)} provided" - {
            Mockito.reset(auditConnector)
            if (withEntitlement) {
              sut.auditProofOfEntitlement(testNino, testStatus, request, Some(testEntitlement))
            } else {
              sut.auditProofOfEntitlement(testNino, testStatus, request)
            }

            "THEN the auditConnector is called with the ViewProofOfEntitlement Event Type" - {
              verify(auditConnector, times(1))
                .sendExplicitAudit(eqTo(ViewProofOfEntitlementModel.EventType), captor.capture())(
                  any[HeaderCarrier](),
                  any[ExecutionContext](),
                  any[Writes[ViewProofOfEntitlementModel]]()
                )

              val capturedEvent = captor.getValue

              forAll(
                Table(
                  ("fieldName", "fieldValue", "expectedResult"),
                  ("Nino", capturedEvent.nino, testNino),
                  ("Status", capturedEvent.status, testStatus),
                  ("Referrer", capturedEvent.referrer, testReferrerValue),
                  ("Device Fingerprint", capturedEvent.deviceFingerprint, testFingerprintValue)
                )
              ) { (fieldName, fieldValue, expectedResult) =>
                testForCapturedValue(
                  s"event (${withOrWithout(withEntitlement)} entitlement)",
                  fieldName,
                  fieldValue,
                  expectedResult
                )
              }

              val capturedEntitlementDetails: Option[ClaimantEntitlementDetails] =
                capturedEvent.claimantEntitlementDetails
              if (withEntitlement) {
                val entitlementDetails = capturedEntitlementDetails.get

                forAll(
                  Table(
                    ("fieldName", "fieldValue", "expectedResult"),
                    ("Name", entitlementDetails.name, testEntitlement.claimant.name.value),
                    ("Address", entitlementDetails.address, testEntitlement.claimant.fullAddress.toSingleLineString),
                    ("Start date", LocalDate.parse(entitlementDetails.start), testEntitlement.claimant.awardStartDate),
                    ("End date", LocalDate.parse(entitlementDetails.end), testEntitlement.claimant.awardEndDate),
                    ("number of children", entitlementDetails.children.length, testEntitlement.children.length)
                  )
                ) { (fieldName, fieldValue, expectedResult) =>
                  testForCapturedValue("entitlement details", fieldName, fieldValue, expectedResult)
                }

                val capturedChild: Option[Child] = capturedEntitlementDetails.map(_.children.last)
                val child = capturedChild.get

                forAll(
                  Table(
                    ("fieldName", "fieldValue", "expectedResult"),
                    ("Name", child.name, testEntitlement.children.last.name),
                    ("Date of Birth", child.dateOfBirth, testEntitlement.children.last.dateOfBirth),
                    (
                      "relationship Start Date",
                      child.relationshipStartDate,
                      testEntitlement.children.head.relationshipStartDate
                    ),
                    (
                      "relationship End Date",
                      child.relationshipEndDate,
                      testEntitlement.children.head.relationshipEndDate
                    )
                  )
                ) { (fieldName, fieldValue, expectedResult) =>
                  testForCapturedValue(
                    s"claimant child (${withOrWithout(withEntitlement)} entitlement)",
                    fieldName,
                    fieldValue,
                    expectedResult
                  )
                }
              } else {
                "AND the event is sent without entitlement details" in {
                  capturedEntitlementDetails mustBe None
                }
              }
            }
          }
        }
      }
    }
  }

  "auditChangeOfBankAccountDetails" - {
    "WHEN an audit call is made for ChangeOfBankAccountDetails" - {
      Mockito.reset(auditConnector)

      val captor: ArgumentCaptor[ChangeOfBankAccountDetailsModel] =
        ArgumentCaptor.forClass(classOf[ChangeOfBankAccountDetailsModel])

      sut.auditChangeOfBankAccountDetails(testNino, testStatus, optionalDataRequest, testClaimantBankInformation)

      "THEN the auditConnector is called with the ViewProofOfEntitlement Event Type" - {
        verify(auditConnector, times(1))
          .sendExplicitAudit(eqTo(ChangeOfBankAccountDetailsModel.EventType), captor.capture())(
            any[HeaderCarrier](),
            any[ExecutionContext](),
            any[Writes[ChangeOfBankAccountDetailsModel]]()
          )

        val capturedEvent               = captor.getValue
        val capturedPersonalInformation = capturedEvent.personalInformation
        val capturedBankDetails         = capturedEvent.bankDetails
        val capturedViewDetails         = capturedEvent.viewDetails

        val formattedBankAccountInformation =
          ClaimantBankInformationHelper.formatBankAccountInformation(testClaimantBankInformation)

        forAll(
          Table(
            ("fieldName", "fieldValue", "expectedResult"),
            ("Nino", capturedEvent.nino, testNino),
            ("Status", capturedEvent.status, testStatus),
            ("Referrer", capturedEvent.referrer, testReferrerValue),
            ("Device Fingerprint", capturedEvent.deviceFingerprint, testFingerprintValue)
          )
        ) { (fieldName, fieldValue, expectedResult) =>
          testForCapturedValue("event", fieldName, fieldValue, expectedResult)
        }

        forAll(
          Table(
            ("fieldName", "fieldValue", "expectedResult"),
            (
              "Name",
              capturedPersonalInformation.name,
              s"${testClaimantBankInformation.firstForename.value} ${testClaimantBankInformation.surname.value}"
            ),
            ("Date of Birth", capturedPersonalInformation.dateOfBirth, testClaimantBankInformation.dateOfBirth),
            ("Nino", capturedPersonalInformation.nino, testNino)
          )
        ) { (fieldName, fieldValue, expectedResult) =>
          testForCapturedValue("Personal Information", fieldName, fieldValue, expectedResult)
        }

        forAll(
          Table(
            ("fieldName", "fieldValue", "expectedResult"),
            ("First Name", capturedBankDetails.firstname, formattedBankAccountInformation.firstForename.value),
            ("Surname", capturedBankDetails.surname, formattedBankAccountInformation.surname.value),
            (
              "Account Holder Name",
              capturedBankDetails.accountHolderName,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.accountHolderName
            ),
            (
              "Account Number",
              capturedBankDetails.accountNumber,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.bankAccountNumber
            ),
            (
              "Sort Code",
              capturedBankDetails.sortCode,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.sortCode
            ),
            (
              "Building Society Roll Number",
              capturedBankDetails.buildingSocietyRollNumber,
              testClaimantBankInformation.financialDetails.bankAccountInformation.buildingSocietyRollNumber
            )
          )
        ) { (fieldName, fieldValue, expectedResult) =>
          testForCapturedValue("Bank Details", fieldName, fieldValue, expectedResult)
        }

        forAll(
          Table(
            ("fieldName", "fieldValue", "expectedResult"),
            (
              "Account Holder Name",
              capturedViewDetails.accountHolderName,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.accountHolderName.map(_.value).get
            ),
            (
              "Account Number",
              capturedViewDetails.accountNumber,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.bankAccountNumber
                .map(_.number)
                .get
            ),
            (
              "Sort Code",
              capturedViewDetails.sortCode,
              formattedBankAccountInformation.financialDetails.bankAccountInformation.sortCode.map(_.value).get
            )
          )
        ) { (fieldName, fieldValue, expectedResult) =>
          testForCapturedValue("View Details", fieldName, fieldValue, expectedResult)
        }
      }
    }
  }

  "auditPaymentDetails" - {
    "GIVEN a ChildBenefitEntitlement" - {
      "WHEN an audit call is made for PaymentDetails" - {
        Mockito.reset(auditConnector)

        val captor: ArgumentCaptor[ViewPaymentDetailsModel] =
          ArgumentCaptor.forClass(classOf[ViewPaymentDetailsModel])

        sut.auditPaymentDetails(testNino, testStatus, optionalDataRequest, Some(testEntitlement))

        "THEN the auditConnector is called with the ViewPaymentDetails Event Type" - {
          verify(auditConnector, times(1))
            .sendExplicitAudit(eqTo(ViewPaymentDetailsModel.EventType), captor.capture())(
              any[HeaderCarrier](),
              any[ExecutionContext](),
              any[Writes[ViewPaymentDetailsModel]]()
            )

          val capturedEvent         = captor.getValue
          val capturedFinancialInfo = capturedEvent.payments

          forAll(
            Table(
              ("fieldName", "fieldValue", "expectedResult"),
              ("Nino", capturedEvent.nino, testNino),
              ("Status", capturedEvent.status, testStatus),
              ("Referrer", capturedEvent.referrer, testReferrerValue),
              ("Device Fingerprint", capturedEvent.deviceFingerprint, testFingerprintValue),
              (
                "Number of Payments Visible to User",
                capturedEvent.numberOfPaymentsVisibleToUser,
                testEntitlement.claimant.lastPaymentsInfo.length
              )
            )
          ) { (fieldName, fieldValue, expectedResult) =>
            testForCapturedValue("event", fieldName, fieldValue, expectedResult)
          }

          capturedFinancialInfo.zip(testEntitlement.claimant.lastPaymentsInfo).zipWithIndex.foreach {
            case ((resultInfo, expectedInfo), index) =>
              forAll(
                Table(
                  ("fieldName", "fieldValue", "expectedResult"),
                  ("Credit Date", resultInfo.creditDate, expectedInfo.creditDate),
                  ("Credit Amount", resultInfo.creditAmount, expectedInfo.creditAmount)
                )
              ) { (fieldName, fieldValue, expectedResult) =>
                testForCapturedValue(s"Payments ($index)", fieldName, fieldValue, expectedResult)
              }
          }
        }
      }
    }
  }

  "auditFTNAEKickOut" - {
    "fire event" in {
      Mockito.reset(auditConnector)
      val childInfo: FtnaeChildInfo = FtnaeChildInfo(
        ChildReferenceNumber(testCRN),
        FirstForename("John"),
        None,
        Surname("Doe"),
        LocalDate.now(),
        LocalDate.now().plusYears(1)
      )
      val courseDuration: Option[CourseDuration] = Some(CourseDuration.TwoYear)
      val answers: List[FtnaeQuestionAndAnswer] =
        List(
          FtnaeQuestionAndAnswer("Question 1", "Answer A"),
          FtnaeQuestionAndAnswer("Question 2", "Answer B"),
          FtnaeQuestionAndAnswer("Question 3", "Answer C")
        )

      val captor: ArgumentCaptor[FtnaeKickOutModel] =
        ArgumentCaptor.forClass(classOf[FtnaeKickOutModel])

      sut.auditFtnaeKickOut(testNino, testStatus, Some(childInfo), courseDuration, answers)

      verify(auditConnector, times(1))
        .sendExplicitAudit(eqTo(FtnaeKickOutModel.EventType), captor.capture())(
          any[HeaderCarrier](),
          any[ExecutionContext](),
          any[Writes[FtnaeKickOutModel]]()
        )

      val capturedEvent = captor.getValue

      capturedEvent.nino mustBe testNino
      capturedEvent.status mustBe testStatus
      capturedEvent.crn mustBe Some(childInfo.crn.value)
      capturedEvent.courseDuration mustBe Some("TWO_YEAR")
      capturedEvent.dateOfBirth mustBe Some(childInfo.dateOfBirth.toString)
      capturedEvent.name mustBe Some(s"${childInfo.name.value} ${childInfo.lastName.value}")
      capturedEvent.answers.length mustEqual answers.length
      capturedEvent.answers mustEqual answers
    }
  }
}
