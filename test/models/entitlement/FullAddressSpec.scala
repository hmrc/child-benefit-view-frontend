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

package models.entitlement

import base.BaseSpec
import models.common.{AddressLine, AddressPostcode}
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class FullAddressSpec extends BaseSpec {

  "FullAddress" - {
    "GIVEN a valid first address line, second address line and an address postCode" - {
      forAll(trueFalseCases) { withAddress3 =>
        forAll(trueFalseCases) { withAddress4 =>
          forAll(trueFalseCases) { withAddress5 =>
            s"AND a third address line ${isOrIsNot(withAddress3)} provided - a fourth address line ${isOrIsNot(withAddress4)} provided - a fifth address line ${isOrIsNot(withAddress5)} provided" - {
              "THEN the expected FullAddress is returned" in {
                forAll(
                  arbitrary[AddressLine],
                  arbitrary[AddressLine],
                  arbitrary[AddressLine],
                  arbitrary[AddressLine],
                  arbitrary[AddressLine],
                  arbitrary[AddressPostcode]
                ) { (addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressPostcode) =>
                  val result = FullAddress(
                    addressLine1,
                    addressLine2,
                    if (withAddress3) Some(addressLine3) else None,
                    if (withAddress4) Some(addressLine4) else None,
                    if (withAddress5) Some(addressLine5) else None,
                    addressPostcode
                  )

                  result.addressLine1 mustBe addressLine1
                  result.addressLine2 mustBe addressLine2
                  result.addressLine3 mustBe (if (withAddress3) Some(addressLine3) else None)
                  result.addressLine4 mustBe (if (withAddress4) Some(addressLine4) else None)
                  result.addressLine5 mustBe (if (withAddress5) Some(addressLine5) else None)
                  result.addressPostcode mustBe addressPostcode
                }
              }
            }
          }
        }
      }
    }
    "format: should successfully format to JSON" in {
      forAll(
        arbitrary[AddressLine],
        arbitrary[AddressLine],
        arbitrary[AddressLine],
        arbitrary[AddressLine],
        arbitrary[AddressLine],
        arbitrary[AddressPostcode]
      ) { (addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressPostcode) =>
        val fullAddress = FullAddress(
          addressLine1,
          addressLine2,
          Some(addressLine3),
          Some(addressLine4),
          Some(addressLine5),
          addressPostcode
        )
        Json.toJson(fullAddress)
      }
    }
    "toSingleLineString" - {
      def expectedSingleLineString(
          fullAddress:  FullAddress,
          withAddress3: Boolean,
          withAddress4: Boolean,
          withAddress5: Boolean
      ): String =
        s"${fullAddress.addressLine1.value} ${fullAddress.addressLine2.value} " +
          s"${if (withAddress3) s"${fullAddress.addressLine3.fold("-Missing Expected Address Line: 3")(_.value)} "
          else ""}" +
          s"${if (withAddress4) s"${fullAddress.addressLine4.fold("-Missing Expected Address Line: 4")(_.value)} "
          else ""}" +
          s"${if (withAddress5) s"${fullAddress.addressLine5.fold("-Missing Expected Address Line: 5")(_.value)} "
          else ""}" +
          s"${fullAddress.addressPostcode.value}"
      "GIVEN a valid first address line, second address line and an address postCode" - {
        forAll(trueFalseCases) { withAddress3 =>
          forAll(trueFalseCases) { withAddress4 =>
            forAll(trueFalseCases) { withAddress5 =>
              s"AND a third address line ${isOrIsNot(withAddress3)} provided - a fourth address line ${isOrIsNot(withAddress4)} provided - a fifth address line ${isOrIsNot(withAddress5)} provided" - {
                "THEN the expected FullAddress is returned" in {
                  forAll(
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressPostcode]
                  ) { (addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressPostcode) =>
                    val fullAddress = FullAddress(
                      addressLine1,
                      addressLine2,
                      if (withAddress3) Some(addressLine3) else None,
                      if (withAddress4) Some(addressLine4) else None,
                      if (withAddress5) Some(addressLine5) else None,
                      addressPostcode
                    )
                    val expectedResult = expectedSingleLineString(fullAddress, withAddress3, withAddress4, withAddress5)

                    val result = fullAddress.toSingleLineString

                    result mustBe expectedResult
                  }
                }
              }
            }
          }
        }
      }
    }
    "toPageDisplayString" - {
      def expectedPageDisplayString(
          fullAddress:  FullAddress,
          withAddress3: Boolean,
          withAddress4: Boolean,
          withAddress5: Boolean
      ): String =
        s"${fullAddress.addressLine1.value}</br>${fullAddress.addressLine2.value}</br>" +
          s"${if (withAddress3) s"${fullAddress.addressLine3.fold("-Missing Expected Address Line: 3")(_.value)}</br>"
          else ""}" +
          s"${if (withAddress4) s"${fullAddress.addressLine4.fold("-Missing Expected Address Line: 4")(_.value)}</br>"
          else ""}" +
          s"${if (withAddress5) s"${fullAddress.addressLine5.fold("-Missing Expected Address Line: 5")(_.value)}</br>"
          else ""}" +
          s"${fullAddress.addressPostcode.value}</br>"
      "GIVEN a valid first address line, second address line and an address postCode" - {
        forAll(trueFalseCases) { withAddress3 =>
          forAll(trueFalseCases) { withAddress4 =>
            forAll(trueFalseCases) { withAddress5 =>
              s"AND a third address line ${isOrIsNot(withAddress3)} provided - a fourth address line ${isOrIsNot(withAddress4)} provided - a fifth address line ${isOrIsNot(withAddress5)} provided" - {
                "THEN the expected FullAddress is returned" in {
                  forAll(
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressLine],
                    arbitrary[AddressPostcode]
                  ) { (addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressPostcode) =>
                    val fullAddress = FullAddress(
                      addressLine1,
                      addressLine2,
                      if (withAddress3) Some(addressLine3) else None,
                      if (withAddress4) Some(addressLine4) else None,
                      if (withAddress5) Some(addressLine5) else None,
                      addressPostcode
                    )
                    val expectedResult =
                      expectedPageDisplayString(fullAddress, withAddress3, withAddress4, withAddress5)

                    val result = fullAddress.toPageDisplayString

                    result mustBe expectedResult
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
