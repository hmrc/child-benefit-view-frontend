/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, JsValue, Writes}

case class IndividualDetails(nino:                          String,
                             ninoSuffix:                    Option[String],
                             accountStatusType:             Option[Int],
                             sex:                           Option[String],
                             dateOfEntry:                   Option[String],
                             dateOfBirth:                   Option[String],
                             dateOfBirthStatus:             Option[Int],
                             dateOfDeath:                   Option[String],
                             dateOfDeathStatus:             Option[Int],
                             dateOfRegistration:            Option[String],
                             registrationType:              Option[Int],
                             adultRegSerialNumber:          Option[String],
                             cesaAgentIdentifier:           Option[String],
                             cesaAgentClientReference:      Option[String],
                             permanentTSuffixCaseIndicator: Option[Int],
                             currOptimisticLock:            Option[Int],
                             liveCapacitorInd:              Option[Int],
                             liveAgentInd:                  Option[Int],
                             ntTaxCodeInd:                  Option[Int],
                             mergeStatus:                   Option[Int],
                             marriageStatusType:            Option[Int],
                             crnIndicator:                  Option[Int],
                             nameList:                      Option[NameList],
                             addressList:                   Option[AddressList],
                             indicators:                    Option[Indicators],
                             residencyList:                 Option[ResidencyList])

object IndividualDetails {
  implicit val format: Format[IndividualDetails] = {
    val builder1 =
      (JsPath \ "nino").format[String] and
        (JsPath \ "ninoSuffix").formatNullable[String] and
        (JsPath \ "accountStatusType").formatNullable[Int] and
        (JsPath \ "sex").formatNullable[String] and
        (JsPath \ "dateOfEntry").formatNullable[String] and
        (JsPath \ "dateOfBirth").formatNullable[String] and
        (JsPath \ "dateOfBirthStatus").formatNullable[Int] and
        (JsPath \ "dateOfDeath").formatNullable[String] and
        (JsPath \ "dateOfDeathStatus").formatNullable[Int] and
        (JsPath \ "dateOfRegistration").formatNullable[String] and
        (JsPath \ "registrationType").formatNullable[Int] and
        (JsPath \ "adultRegSerialNumber").formatNullable[String] and
        (JsPath \ "cesaAgentIdentifier").formatNullable[String] and
        (JsPath \ "cesaAgentClientReference").formatNullable[String] and
        (JsPath \ "permanentTSuffixCaseIndicator").formatNullable[Int] and
        (JsPath \ "currOptimisticLock").formatNullable[Int] and
        (JsPath \ "liveCapacitorInd").formatNullable[Int] and
        (JsPath \ "liveAgentInd").formatNullable[Int] and
        (JsPath \ "ntTaxCodeInd").formatNullable[Int] and
        (JsPath \ "mergeStatus").formatNullable[Int] and
        (JsPath \ "marriageStatusType").formatNullable[Int] and
        (JsPath \ "crnIndicator").formatNullable[Int]

    val builder2 =
      (JsPath \ "nameList").formatNullable[NameList] and
        (JsPath \ "addressList").formatNullable[AddressList] and
        (JsPath \ "indicators").formatNullable[Indicators] and
        (JsPath \ "residencyList").formatNullable[ResidencyList]

    val format1 = builder1.apply[IndividualDetails](
      (nino: String,
        ninoSuffix: Option[String],
        accountStatusType: Option[Int],
        sex: Option[String],
        dateOfEntry: Option[String],
        dateOfBirth: Option[String],
        dateOfBirthStatus: Option[Int],
        dateOfDeath: Option[String],
        dateOfDeathStatus: Option[Int],
        dateOfRegistration: Option[String],
        registrationType: Option[Int],
        adultRegSerialNumber: Option[String],
        cesaAgentIdentifier: Option[String],
        cesaAgentClientReference: Option[String],
        permanentTSuffixCaseIndicator: Option[Int],
        currOptimisticLock: Option[Int],
        liveCapacitorInd: Option[Int],
        liveAgentInd: Option[Int],
        ntTaxCodeInd: Option[Int],
        mergeStatus: Option[Int],
        marriageStatusType: Option[Int],
        crnIndicator: Option[Int]) =>
        IndividualDetails(
          nino,
          ninoSuffix,
          accountStatusType,
          sex,
          dateOfEntry,
          dateOfBirth,
          dateOfBirthStatus,
          dateOfDeath,
          dateOfDeathStatus,
          dateOfRegistration,
          registrationType,
          adultRegSerialNumber,
          cesaAgentIdentifier,
          cesaAgentClientReference,
          permanentTSuffixCaseIndicator,
          currOptimisticLock,
          liveCapacitorInd,
          liveAgentInd,
          ntTaxCodeInd,
          mergeStatus,
          marriageStatusType,
          crnIndicator,
          None,
          None,
          None,
          None
        ),
      (i: IndividualDetails) =>
        (
          i.nino,
          i.ninoSuffix,
          i.accountStatusType,
          i.sex,
          i.dateOfEntry,
          i.dateOfBirth,
          i.dateOfBirthStatus,
          i.dateOfDeath,
          i.dateOfDeathStatus,
          i.dateOfRegistration,
          i.registrationType,
          i.adultRegSerialNumber,
          i.cesaAgentIdentifier,
          i.cesaAgentClientReference,
          i.permanentTSuffixCaseIndicator,
          i.currOptimisticLock,
          i.liveCapacitorInd,
          i.liveAgentInd,
          i.ntTaxCodeInd,
          i.mergeStatus,
          i.marriageStatusType,
          i.crnIndicator
        )
    )

    val format2 = builder2.apply[IndividualDetails](
      (nameList: Option[NameList],
        addressList: Option[AddressList],
        indicators: Option[Indicators],
        residencyList: Option[ResidencyList]) =>
        IndividualDetails(
          "",
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          nameList,
          addressList,
          indicators,
          residencyList
        ),
      (i: IndividualDetails) =>
        (i.nameList, i.addressList, i.indicators, i.residencyList)
    )

    val reads = format1.flatMap { i1 =>
      format2.map { i2 =>
        i1.copy(
          nameList      = i2.nameList,
          addressList   = i2.addressList,
          indicators    = i2.indicators,
          residencyList = i2.residencyList
        )
      }
    }

    val writes = new Writes[IndividualDetails] {
      override def writes(i1: IndividualDetails): JsValue =
        format2
          .contramap[IndividualDetails] { i2 =>
            i1.copy(
              nameList      = i2.nameList,
              addressList   = i2.addressList,
              indicators    = i2.indicators,
              residencyList = i2.residencyList
            )
          }
          .writes(i1)
    }

    Format(reads, writes)
  }
}
