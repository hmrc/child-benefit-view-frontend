/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import base.BaseAppSpec
import config.FrontendAppConfig
import izumi.reflect.Tag
import models.changeofbank.AccountHolderType.Claimant
import models.changeofbank.{AccountHolderName, BankAccountNumber, BankDetails, ClaimantBankAccountInformation, ClaimantBankInformation, ClaimantFinancialDetails, SortCode}
import models.cob.{UpdateBankAccountRequest, UpdateBankDetailsResponse, VerifyBankAccountRequest}
import models.common.{FirstForename, Surname}
import models.errors.{CBError, ClaimantIsLockedOutOfChangeOfBank, ConnectorError, PriorityBARSVerificationError}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.http.Status.{FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.BodyWritable
import stubs.ChildBenefitServiceStubs._
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, UpstreamErrorResponse}
import utils.TestData._

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ChangeOfBankConnectorSpec extends BaseAppSpec with GuiceOneAppPerSuite {
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val hc: HeaderCarrier    = new HeaderCarrier()

  stopWireMock()
  startWireMock()

  override implicit lazy val app: Application           = applicationBuilder().build()
  val sutWithStubs:               ChangeOfBankConnector = app.injector.instanceOf[ChangeOfBankConnector]

  val mockHttpClient: HttpClientV2          = mock[HttpClientV2]
  val requestBuilder: RequestBuilder        = mock[RequestBuilder]
  val mockAppConfig:  FrontendAppConfig     = mock[FrontendAppConfig]
  val sutWithMocks:   ChangeOfBankConnector = new ChangeOfBankConnector(mockHttpClient, mockAppConfig)

  def connector: ChangeOfBankConnector = {
    val injector: Injector = GuiceApplicationBuilder()
      .overrides(
        inject.bind[RequestBuilder].toInstance(requestBuilder),
        inject.bind[HttpClientV2].toInstance(mockHttpClient)
      )
      .injector()

    injector.instanceOf[ChangeOfBankConnector]
  }

  val message = "error message"

  val errors: Seq[Int] = Seq(
    NOT_FOUND,
    INTERNAL_SERVER_ERROR
  )

  private val claimantBankInformation: ClaimantBankInformation = ClaimantBankInformation(
    firstForename = FirstForename("Name"),
    surname = Surname("Surname"),
    dateOfBirth = LocalDate.now().minusYears(20),
    activeChildBenefitClaim = true,
    financialDetails = ClaimantFinancialDetails(
      awardEndDate = LocalDate.now().plusMonths(8),
      adjustmentReasonCode = None,
      adjustmentEndDate = None,
      bankAccountInformation = ClaimantBankAccountInformation(
        accountHolderName = Some(AccountHolderName("Name")),
        sortCode = Some(SortCode("00-11-22")),
        bankAccountNumber = Some(BankAccountNumber("12345678")),
        buildingSocietyRollNumber = None
      )
    )
  )

  private val verifyBankAccountRequest: VerifyBankAccountRequest = VerifyBankAccountRequest(
            accountHolderName = AccountHolderName("A Name"),
            sortCode = SortCode("00-11-22"),
            bankAccount = BankAccountNumber("12345678")
          )

  private val updateBankAccountRequest: UpdateBankAccountRequest = UpdateBankAccountRequest(
            updatedBankInformation = BankDetails(
              accountHolderType = Claimant,
              accountHolderName = AccountHolderName("A Name"),
              accountNumber = BankAccountNumber("12345678"),
              sortCode = SortCode("00-11-22")
            )
          )

  when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
    .thenReturn(requestBuilder)

//  restartWiremock()
  "getChangeOfBankClaimantInfo" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the ClaimantBankInformation in the response is returned" in {
          changeOfBankUserInfoStub(claimantBankInformation)

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe Right(claimantBankInformation)
          }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a lockedOutError response" - {
        "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
          changeOfBankUserInfoFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
          }
        }
      }
      "AND the response matches a barsFailureError response" - {
        "THEN an expected PriorityBARSVerificationError is returned" in {
          changeOfBankUserInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
          }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          changeOfBankUserInfoFailureStub(
            INTERNAL_SERVER_ERROR,
            genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
          )

          whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }

//    restartWiremock()
    errors.foreach { responseCode =>
      "GIVEN the HttpClient experiences an exception" - {
        when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
        s"AND the exception is of type $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {

            when(
              requestBuilder.execute[Either[CBError, ClaimantBankInformation]](
                any[HttpReads[Either[CBError, ClaimantBankInformation]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {

            when(
              requestBuilder.execute[Either[CBError, ClaimantBankInformation]](
                any[HttpReads[Either[CBError, ClaimantBankInformation]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.getChangeOfBankClaimantInfo.value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        changeOfBankUserInfoFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
          result mustBe a[Left[_, ClaimantBankInformation]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
      }
    }

    "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
      "THEN an expected ConnectorError is returned" in {
        changeOfBankUserInfoFailureStub(OK, validNotMatchingJsonResponse)

        whenReady(sutWithStubs.getChangeOfBankClaimantInfo.value) { result =>
          result mustBe a[Left[_, ClaimantBankInformation]]
          result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
        }
      }
    }
  }

  "verifyClaimantBankAccount" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the a Unit response is returned" in {
          verifyClaimantBankInfoStub()

          whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
            result mustBe Right(())
          }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a lockedOutError response" - {
        "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
            verifyClaimantBankInfoFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

            whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
            }
        }
      }
      "AND the response matches a barsFailureError response" - {
        "THEN an expected PriorityBARSVerificationError is returned" in {
            verifyClaimantBankInfoFailureStub(NOT_FOUND, barsFailureErrorResponse)

            whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
            }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
            val expectedMessage = "Unit Test other failure expected message"
            verifyClaimantBankInfoFailureStub(
              INTERNAL_SERVER_ERROR,
              genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
            )

            whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
            }
       }
      }
    }

//    restartWiremock()

    "GIVEN the HttpClient experiences an exception" - {
      errors.foreach { responseCode =>
        s"AND the exception is of type HttpException: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {

            when(
              requestBuilder.withBody(any[VerifyBankAccountRequest])(
                any[BodyWritable[VerifyBankAccountRequest]],
                any[Tag[VerifyBankAccountRequest]],
                any[ExecutionContext]
              )
            ).thenReturn(requestBuilder)

            when(
              requestBuilder.execute[Either[CBError, Unit]](any[HttpReads[Either[CBError, Unit]]], any[ExecutionContext])
            ).thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }

        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {

            when(
              requestBuilder.withBody(any[VerifyBankAccountRequest])(
                any[BodyWritable[VerifyBankAccountRequest]],
                any[Tag[VerifyBankAccountRequest]],
                any[ExecutionContext]
              )
            ).thenReturn(requestBuilder)

            when(
              requestBuilder.execute[Either[CBError, Unit]](
                any[HttpReads[Either[CBError, Unit]]],
                any[ExecutionContext]
              )
            ).thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
          verifyClaimantBankAccountFailureStub(OK, invalidJsonResponse)

          whenReady(sutWithStubs.verifyClaimantBankAccount(verifyBankAccountRequest).value) { result =>
            result mustBe a[Left[_, Unit]]
            result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
          }
      }
    }
  }

//  restartWiremock()
  "verifyBARNotLocked" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the a Unit response is returned" in {
        verifyBARNotLockedStub()

        whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
          result mustBe Right(())
        }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a lockedOutError response" - {
        "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
          verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

          whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
            result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
          }
        }
      }
      "AND the response matches a barsFailureError response" - {
        "THEN an expected PriorityBARSVerificationError is returned" in {
          verifyBARNotLockedFailureStub(NOT_FOUND, barsFailureErrorResponse)

          whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
            result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
          }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          verifyBARNotLockedFailureStub(INTERNAL_SERVER_ERROR, genericCBError(INTERNAL_SERVER_ERROR, expectedMessage))

          whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }

//    restartWiremock()
    "GIVEN the HttpClient experiences an exception" - {
      errors.foreach { responseCode =>
        s"AND the exception is of type HttpException: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

            when(
              requestBuilder
                .execute[Either[CBError, Unit]](any[HttpReads[Either[CBError, Unit]]], any[ExecutionContext])
            )
              .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.verifyBARNotLocked().value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }

        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(mockHttpClient.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

            when(
              requestBuilder.execute[Either[CBError, Unit]](
                any[HttpReads[Either[CBError, Unit]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.verifyBARNotLocked().value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

//    restartWiremock()
    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        verifyBARNotLockedFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.verifyBARNotLocked().value) { result =>
          result mustBe a[Left[_, Unit]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
      }
    }
  }

//  restartWiremock()
  "updateBankAccount" - {
//    restartWiremock()
    "GIVEN the HttpClient experiences an exception" - {
     errors.foreach { responseCode =>
        s"AND the exception is of type HttpException: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
              .thenReturn(requestBuilder)

            when(
              requestBuilder
                .withBody(any[UpdateBankAccountRequest])(
                  any[BodyWritable[UpdateBankAccountRequest]],
                  any[Tag[UpdateBankAccountRequest]],
                  any[ExecutionContext]
                )
            )
              .thenReturn(requestBuilder)

            when(
              requestBuilder.execute[Either[CBError, UpdateBankDetailsResponse]](
                any[HttpReads[Either[CBError, UpdateBankDetailsResponse]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.successful(Left(ConnectorError(responseCode, message))))

            whenReady(connector.updateBankAccount(updateBankAccountRequest).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
        s"AND the exception is of type UpstreamErrorResponse: $responseCode" - {
          "THEN a ConnectorError is returned with the matching details" in {
            when(mockHttpClient.put(any[URL])(any[HeaderCarrier]))
              .thenReturn(requestBuilder)

            when(
              requestBuilder
                .withBody(any[UpdateBankAccountRequest])(
                  any[BodyWritable[UpdateBankAccountRequest]],
                  any[Tag[UpdateBankAccountRequest]],
                  any[ExecutionContext]
                )
            )
              .thenReturn(requestBuilder)

            when(
              requestBuilder.execute[Either[CBError, UpdateBankDetailsResponse]](
                any[HttpReads[Either[CBError, UpdateBankDetailsResponse]]],
                any[ExecutionContext]
              )
            )
              .thenReturn(Future.failed(UpstreamErrorResponse(message, responseCode)))

            whenReady(connector.updateBankAccount(updateBankAccountRequest).value) { result =>
              result mustBe Left(ConnectorError(responseCode, message))
            }
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response" - {
      "THEN the a Unit response is returned" in {
        updateBankAccountStub(UpdateBankDetailsResponse("Success"))

        whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
          result mustBe Right(UpdateBankDetailsResponse("Success"))
        }
      }
    }
    "GIVEN the HttpClient receives a CBErrorResponse" - {
      "AND the response matches a lockedOutError response" - {
        "THEN an expected ClaimantIsLockedOutOfChangeOfBank is returned" in {
          updateBankAccountFailureStub(INTERNAL_SERVER_ERROR, lockedOutErrorResponse)

          whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
            result mustBe Left(ClaimantIsLockedOutOfChangeOfBank(FORBIDDEN, lockedOutErrorDescription))
          }
        }
      }
      "AND the response matches a barsFailureError response" - {
        "THEN an expected PriorityBARSVerificationError is returned" in {
          updateBankAccountFailureStub(NOT_FOUND, barsFailureErrorResponse)

          whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
            result mustBe Left(PriorityBARSVerificationError(NOT_FOUND, barsFailureErrorDescription))
          }
        }
      }
      "AND the response does not match any predetermined CBErrorResponse" - {
        "THEN a ConnectorError with the matched status and description is returned" in {
          val expectedMessage = "Unit Test other failure expected message"
          updateBankAccountFailureStub(
            INTERNAL_SERVER_ERROR,
            genericCBError(INTERNAL_SERVER_ERROR, expectedMessage)
          )

          whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, expectedMessage))
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        updateBankAccountFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
          result mustBe a[Left[_, UpdateBankDetailsResponse]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
      }
    }
    "GIVEN the HttpClient received a successful response that is valid Json but does not validate as the return type" - {
      "THEN an expected ConnectorError is returned" in {
        updateBankAccountFailureStub(OK, validNotMatchingJsonResponse)

        whenReady(sutWithStubs.updateBankAccount(updateBankAccountRequest).value) { result =>
          result mustBe a[Left[_, UpdateBankDetailsResponse]]
          result.left.map(error => error.statusCode mustBe SERVICE_UNAVAILABLE)
        }
      }
    }
  }

  "dropChangeOfBankCache" - {
    "GIVEN the HttpClient receives a successful response" - {
      "THEN the a Unit response is returned" in {
        dropChangeOfBankStub()

        whenReady(sutWithStubs.dropChangeOfBankCache().value) { result =>
          result mustBe Right(())
        }
      }
    }

    "GIVEN the HttpClient experiences an exception" - {
      "AND the exception is of type HttpException" - {
        "THEN a ConnectorError is returned with the matching details" in {
          when(mockHttpClient.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

          when(
            requestBuilder.execute[Either[CBError, Unit]](
              any[HttpReads[Either[CBError, Unit]]],
              any[ExecutionContext]
            )
          ).thenReturn(Future.successful(Left(ConnectorError(INTERNAL_SERVER_ERROR, message))))

          whenReady(connector.dropChangeOfBankCache().value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, message))
          }
        }
      }
      "AND the exception is of type UpstreamErrorResponse" - {
        "THEN a ConnectorError is returned with the matching details" in {
          when(mockHttpClient.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)

          when(
            requestBuilder.execute[Either[CBError, Unit]](
              any[HttpReads[Either[CBError, Unit]]],
              any[ExecutionContext]
            )
          ).thenReturn(Future.failed(UpstreamErrorResponse(message, INTERNAL_SERVER_ERROR)))

          whenReady(connector.dropChangeOfBankCache().value) { result =>
            result mustBe Left(ConnectorError(INTERNAL_SERVER_ERROR, message))
          }
        }
      }
    }

    "GIVEN the HttpClient receives a successful response that is invalid Json" - {
      "THEN an expected ConnectorError is returned" in {
        dropChangeOfBankFailureStub(OK, invalidJsonResponse)

        whenReady(sutWithStubs.dropChangeOfBankCache().value) { result =>
          result mustBe a[Left[_, Unit]]
          result.left.map(error => error.statusCode mustBe INTERNAL_SERVER_ERROR)
        }
      }
    }
  }
}
