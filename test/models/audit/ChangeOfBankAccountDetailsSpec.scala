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

package models.audit

import base.BaseSpec
import models.changeofbank.{AccountHolderName, BankAccountNumber, BuildingSocietyRollNumber, SortCode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{alphaNumStr, alphaStr}
import play.api.libs.json.Json

import java.time.LocalDate

class ChangeOfBankAccountDetailsSpec extends BaseSpec {
  "PersonalInformation" - {
    "GIVEN a valid name, date of birth and nino" - {
      "THEN the expected PersonalInformation is returned" in {
        forAll(generateName, arbitrary[LocalDate], generateNino) { (name, dateOfBirth, nino) =>
          val result = PersonalInformation(name, dateOfBirth, nino)

          result.name mustBe name
          result.dateOfBirth mustBe dateOfBirth
          result.nino mustBe nino
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName, arbitrary[LocalDate], generateNino) { (name, dateOfBirth, nino) =>
        val personalInformation = PersonalInformation(name, dateOfBirth, nino)
        Json.toJson(personalInformation)
      }
    }
  }
  "BankDetails" - {
    "GIVEN a valid first name and surname" - {
      forAll(trueFalseCases) { withAccountHolderName =>
        forAll(trueFalseCases) { withAccountNumber =>
          forAll(trueFalseCases) { withSortCode =>
            forAll(trueFalseCases) { withBuildingSocietyNumber =>
              s"AND account holder name ${isOrIsNot(withAccountHolderName)} provided - account number ${isOrIsNot(
                withAccountNumber
              )} provided - sort code ${isOrIsNot(withSortCode)} provided - building society number ${isOrIsNot(withBuildingSocietyNumber)} provided" - {
                "THEN the expected BankDetails is returned" in {
                  forAll(
                    generateName,
                    generateName,
                    arbitrary[AccountHolderName],
                    arbitrary[BankAccountNumber],
                    arbitrary[SortCode],
                    arbitrary[BuildingSocietyRollNumber]
                  ) { (firstname, surname, accountHolderName, bankAccountNumber, sortCode, buildingSocietyRollNumber) =>
                    val result = BankDetails(
                      firstname,
                      surname,
                      if (withAccountHolderName) Some(accountHolderName) else None,
                      if (withAccountNumber) Some(bankAccountNumber) else None,
                      if (withSortCode) Some(sortCode) else None,
                      if (withBuildingSocietyNumber) Some(buildingSocietyRollNumber) else None
                    )

                    result.firstname mustBe firstname
                    result.surname mustBe surname
                    result.accountHolderName mustBe (if (withAccountHolderName) Some(accountHolderName) else None)
                    result.accountNumber mustBe (if (withAccountNumber) Some(bankAccountNumber) else None)
                    result.sortCode mustBe (if (withSortCode) Some(sortCode) else None)
                    result.buildingSocietyRollNumber mustBe (if (withBuildingSocietyNumber)
                                                               Some(buildingSocietyRollNumber)
                                                             else None)
                  }
                }
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        generateName,
        generateName,
        arbitrary[AccountHolderName],
        arbitrary[BankAccountNumber],
        arbitrary[SortCode],
        arbitrary[BuildingSocietyRollNumber]
      ) { (firstname, surname, accountHolderName, bankAccountNumber, sortCode, buildingSocietyRollNumber) =>
        val bankDetails = BankDetails(
          firstname,
          surname,
          Some(accountHolderName),
          Some(bankAccountNumber),
          Some(sortCode),
          Some(buildingSocietyRollNumber)
        )
        Json.toJson(bankDetails)
      }
    }
  }
  "ViewDetails" - {
    "GIVEN a valid account holder name, account number and sort code" - {
      "THEN the expected ViewDetails is returned" in {
        forAll(generateName, generateAccountNumber, generateSortCode) { (accountHolderName, accountNumber, sortCode) =>
          val result = ViewDetails(accountHolderName, accountNumber, sortCode)

          result.accountHolderName mustBe accountHolderName
          result.sortCode mustBe sortCode
          result.accountNumber mustBe accountNumber
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(generateName, generateAccountNumber, generateSortCode) { (accountHolderName, accountNumber, sortCode) =>
        val viewDetails = ViewDetails(accountHolderName, accountNumber, sortCode)
        Json.toJson(viewDetails)
      }
    }
  }
  "ChangeOfBankAccountDetailsModel" - {
    "GIVEN a valid nino, status, referrer, device fingerprint, personal information, back details and view details" - {
      "THEN the expected ChangeOfBankAccountDetailsModel is returned" in {
        forAll(
          generateNino,
          alphaStr,
          alphaNumStr,
          arbitrary[PersonalInformation],
          arbitrary[BankDetails],
          arbitrary[ViewDetails]
        ) { (nino, statusOrReferrer, deviceFingerprint, personalInformation, bankDetails, viewDetails) =>
          val result = ChangeOfBankAccountDetailsModel(
            nino,
            statusOrReferrer,
            statusOrReferrer,
            deviceFingerprint,
            personalInformation,
            bankDetails,
            viewDetails
          )

          result.nino mustBe nino
          result.status mustBe statusOrReferrer
          result.referrer mustBe statusOrReferrer
          result.deviceFingerprint mustBe deviceFingerprint
          result.personalInformation mustBe personalInformation
          result.bankDetails mustBe bankDetails
          result.viewDetails mustBe viewDetails
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        generateNino,
        alphaStr,
        alphaNumStr,
        arbitrary[PersonalInformation],
        arbitrary[BankDetails],
        arbitrary[ViewDetails]
      ) { (nino, statusOrReferrer, deviceFingerprint, personalInformation, bankDetails, viewDetails) =>
        val changeOfBankAccountDetailsModel = ChangeOfBankAccountDetailsModel(
          nino,
          statusOrReferrer,
          statusOrReferrer,
          deviceFingerprint,
          personalInformation,
          bankDetails,
          viewDetails
        )
        Json.toJson(changeOfBankAccountDetailsModel)
      }
    }
  }
}
