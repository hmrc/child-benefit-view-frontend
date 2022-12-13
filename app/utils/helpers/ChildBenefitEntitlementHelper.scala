/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.helpers

import models.common.{AddressLine, AddressPostcode}
import models.entitlement.{ChildBenefitEntitlement, FullName}
import utils.helpers.StringHelper.toTitleCase

object ChildBenefitEntitlementHelper {

  def formatChildBenefitEntitlement(entitlement: ChildBenefitEntitlement): ChildBenefitEntitlement =
    entitlement.copy(
      claimant = entitlement.claimant.copy(
        name = FullName(toTitleCase(entitlement.claimant.name.value)),
        fullAddress = entitlement.claimant.fullAddress.copy(
          addressLine1 = AddressLine(
            toTitleCase(entitlement.claimant.fullAddress.addressLine1.value)
          ),
          addressLine2 = AddressLine(
            toTitleCase(entitlement.claimant.fullAddress.addressLine2.value)
          ),
          addressLine3 = entitlement.claimant.fullAddress.addressLine3.fold[Option[AddressLine]](None)(al3 =>
            Some(AddressLine(toTitleCase(al3.value)))
          ),
          addressLine4 = entitlement.claimant.fullAddress.addressLine4.fold[Option[AddressLine]](None)(al4 =>
            Some(AddressLine(toTitleCase(al4.value)))
          ),
          addressLine5 = entitlement.claimant.fullAddress.addressLine5.fold[Option[AddressLine]](None)(al5 =>
            Some(AddressLine(toTitleCase(al5.value)))
          ),
          addressPostcode = AddressPostcode(entitlement.claimant.fullAddress.addressPostcode.value.toUpperCase)
        )
      ),
      children = entitlement.children.map(child => child.copy(name = FullName(toTitleCase(child.name.value))))
    )
}
