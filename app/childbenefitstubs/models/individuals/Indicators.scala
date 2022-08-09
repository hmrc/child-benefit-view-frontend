/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package childbenefitstubs.models.individuals

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, JsValue, Writes}

import java.time.LocalDate

case class Indicators(manualCodingInd:        Option[Int],
                      manualCodingReason:     Option[Int],
                      manualCodingOther:      Option[String],
                      manualCorrInd:          Option[Int],
                      manualCorrReason:       Option[String],
                      additionalNotes:        Option[String],
                      deceasedInd:            Option[Int],
                      s128Ind:                Option[Int],
                      noAllowInd:             Option[Int],
                      eeaCmnwthInd:           Option[Int],
                      noRepaymentInd:         Option[Int],
                      saLinkInd:              Option[Int],
                      noATSInd:               Option[Int],
                      taxEqualBenInd:         Option[Int],
                      p2ToAgentInd:           Option[Int],
                      digitallyExcludedInd:   Option[Int],
                      bankruptcyInd:          Option[Int],
                      bankruptcyFiledDate:    Option[LocalDate],
                      utr:                    Option[String],
                      audioOutputInd:         Option[Int],
                      welshOutputInd:         Option[Int],
                      largePrintOutputInd:    Option[Int],
                      brailleOutputInd:       Option[Int],
                      specialistBusinessArea: Option[Int],
                      saStartYear:            Option[String],
                      saFinalYear:            Option[String],
                      digitalP2Ind:           Option[Int])

object Indicators {
  implicit val format: Format[Indicators] = {
    val builder1 =
      (JsPath \ "manualCodingInd").formatNullable[Int] and
        (JsPath \ "manualCodingReason").formatNullable[Int] and
        (JsPath \ "manualCodingOther").formatNullable[String] and
        (JsPath \ "manualCorrInd").formatNullable[Int] and
        (JsPath \ "manualCorrReason").formatNullable[String] and
        (JsPath \ "additionalNotes").formatNullable[String] and
        (JsPath \ "deceasedInd").formatNullable[Int] and
        (JsPath \ "s128Ind").formatNullable[Int] and
        (JsPath \ "noAllowInd").formatNullable[Int] and
        (JsPath \ "eeaCmnwthInd").formatNullable[Int] and
        (JsPath \ "noRepaymentInd").formatNullable[Int] and
        (JsPath \ "saLinkInd").formatNullable[Int] and
        (JsPath \ "noATSInd").formatNullable[Int] and
        (JsPath \ "taxEqualBenInd").formatNullable[Int] and
        (JsPath \ "p2ToAgentInd").formatNullable[Int] and
        (JsPath \ "digitallyExcludedInd").formatNullable[Int] and
        (JsPath \ "bankruptcyInd").formatNullable[Int] and
        (JsPath \ "bankruptcyFiledDate").formatNullable[LocalDate] and
        (JsPath \ "utr").formatNullable[String] and
        (JsPath \ "audioOutputInd").formatNullable[Int] and
        (JsPath \ "welshOutputInd").formatNullable[Int] and
        (JsPath \ "largePrintOutputInd").formatNullable[Int]

    val builder2 =
      (JsPath \ "brailleOutputInd").formatNullable[Int] and
        (JsPath \ "specialistBusinessArea").formatNullable[Int] and
        (JsPath \ "saStartYear").formatNullable[String] and
        (JsPath \ "saFinalYear").formatNullable[String] and
        (JsPath \ "digitalP2Ind").formatNullable[Int]

    val format1 = builder1.apply[Indicators](
      (manualCodingInd: Option[Int],
        manualCodingReason: Option[Int],
        manualCodingOther: Option[String],
        manualCorrInd: Option[Int],
        manualCorrReason: Option[String],
        additionalNotes: Option[String],
        deceasedInd: Option[Int],
        s128Ind: Option[Int],
        noAllowInd: Option[Int],
        eeaCmnwthInd: Option[Int],
        noRepaymentInd: Option[Int],
        saLinkInd: Option[Int],
        noATSInd: Option[Int],
        taxEqualBenInd: Option[Int],
        p2ToAgentInd: Option[Int],
        digitallyExcludedInd: Option[Int],
        bankruptcyInd: Option[Int],
        bankruptcyFiledDate: Option[LocalDate],
        utr: Option[String],
        audioOutputInd: Option[Int],
        welshOutputInd: Option[Int],
        largePrintOutputInd: Option[Int]) =>
        Indicators(
          manualCodingInd,
          manualCodingReason,
          manualCodingOther,
          manualCorrInd,
          manualCorrReason,
          additionalNotes,
          deceasedInd,
          s128Ind,
          noAllowInd,
          eeaCmnwthInd,
          noRepaymentInd,
          saLinkInd,
          noATSInd,
          taxEqualBenInd,
          p2ToAgentInd,
          digitallyExcludedInd,
          bankruptcyInd,
          bankruptcyFiledDate,
          utr,
          audioOutputInd,
          welshOutputInd,
          largePrintOutputInd,
          None,
          None,
          None,
          None,
          None
        ),
      (i: Indicators) =>
        (
          i.manualCodingInd,
          i.manualCodingReason,
          i.manualCodingOther,
          i.manualCorrInd,
          i.manualCorrReason,
          i.additionalNotes,
          i.deceasedInd,
          i.s128Ind,
          i.noAllowInd,
          i.eeaCmnwthInd,
          i.noRepaymentInd,
          i.saLinkInd,
          i.noATSInd,
          i.taxEqualBenInd,
          i.p2ToAgentInd,
          i.digitallyExcludedInd,
          i.bankruptcyInd,
          i.bankruptcyFiledDate,
          i.utr,
          i.audioOutputInd,
          i.welshOutputInd,
          i.largePrintOutputInd
        )
    )

    val format2 = builder2.apply[Indicators](
      (brailleOutputInd: Option[Int],
        specialistBusinessArea: Option[Int],
        saStartYear: Option[String],
        saFinalYear: Option[String],
        digitalP2Ind: Option[Int]) =>
        Indicators(
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
          None,
          brailleOutputInd,
          specialistBusinessArea,
          saStartYear,
          saFinalYear,
          digitalP2Ind
        ),
      (i: Indicators) =>
        (
          i.brailleOutputInd,
          i.specialistBusinessArea,
          i.saStartYear,
          i.saFinalYear,
          i.digitalP2Ind
        )
    )

    val reads = format1.flatMap { i1 =>
      format2.map { i2 =>
        i1.copy(
          brailleOutputInd       = i2.brailleOutputInd,
          specialistBusinessArea = i2.specialistBusinessArea,
          saStartYear            = i2.saStartYear,
          saFinalYear            = i2.saFinalYear,
          digitalP2Ind           = i2.digitalP2Ind
        )
      }
    }

    val writes = new Writes[Indicators] {
      override def writes(i1: Indicators): JsValue =
        format2
          .contramap[Indicators] { i2 =>
            i1.copy(
              brailleOutputInd       = i2.brailleOutputInd,
              specialistBusinessArea = i2.specialistBusinessArea,
              saStartYear            = i2.saStartYear,
              saFinalYear            = i2.saFinalYear,
              digitalP2Ind           = i2.digitalP2Ind
            )
          }
          .writes(i1)
    }

    Format(reads, writes)
  }
}
