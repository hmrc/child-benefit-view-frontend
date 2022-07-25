/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt

import java.time.LocalDate.now

import javax.inject.{Inject, Singleton}
import paymentsstubs.desvrt.model.DesData._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class DesController @Inject() (mcc: MessagesControllerComponents) extends FrontendController(mcc) {
  def customerInformation(vrn: String): Action[AnyContent] = Action {
    if (vrn == "101747696" || vrn == "999964805") Ok(customerDataOkNoBankDetails)
    else if (vrn == "220432715") Ok(customerDataOk())
    else if (vrn.contains("999")) NotFound(customerDataNotFound)
    else if (vrn.contains("X")) Ok(customerDataOkNoBankDetails)
    else if (vrn.contains("W")) Ok(customerDataOkNoBankDetailsInFlight)
    else if (vrn.contains("Y")) Ok(customerDataOkWithPartialBankDetails)
    else if (vrn.contains("A")) Ok(customerDataOk(true))
    else Ok(customerDataOk())
  }

  def ddData(vrn: String): Action[AnyContent] = Action {
    if (vrn.contains("888")) NotFound(ddNotFound)
    else if (vrn == "999964805") NotFound(ddNotFound)
    else if (vrn.contains("Z")) Ok(ddOkNoMandate)
    else Ok(ddOk)
  }

  def financialInformation(vrn: String): Action[AnyContent] = Action {
    if (vrn == "879331565") Ok(customFinancialDataSingleCredit(vrn))
    else if ((vrn == "101747011") || (vrn == "11747011X") || (vrn == "101747014") || (vrn == "11747014X")) Ok(financialDataSingleCredit(vrn))
    else if ((vrn == "101747013") || (vrn == "101747015") || (vrn == "101747016")) Ok(financialDataSingleDebit(vrn))
    else NotFound(financialDataNotFound)
  }

  def repaymentData(vrn: String): Action[AnyContent] = Action {
    if (vrn == "879331565")
      Ok(repaymentDetailsCustom)
    else if ((vrn == "101747696") || (vrn == "220432715" || (vrn == "999964805")))
      Ok(repaymentDetailSingleInProgress)
    else if (vrn == "101747001")
      Ok(repaymentDetails1(now().toString, "SENT_FOR_RISKING"))
    else if (vrn == "101747002")
      Ok(repaymentDetails1(now().toString, "CLAIM_QUERIED"))
    else if (vrn == "101747003")
      Ok(repaymentDetails2(now().toString, "INITIAL", "CLAIM_QUERIED"))
    else if (vrn == "101747004")
      Ok(repaymentDetails2(now().toString, "INITIAL", "SENT_FOR_RISKING"))
    else if (vrn == "101747005")
      Ok(repaymentDetails2(now().minusDays(40).toString, "INITIAL", "CLAIM_QUERIED"))
    else if ((vrn == "101747006") || (vrn == "11747006X"))
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "REPAYMENT_ADJUSTED"))
    else if (vrn == "101747008")
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "ADJUSMENT_TO_TAX_DUE"))
    else if ((vrn == "101747009") || (vrn == "11747009X") || (vrn == "11747014X") || (vrn == "101747014"))
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "REPAYMENT_APPROVED"))
    else if ((vrn == "101747011") || vrn == "11747011X")
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "REPAYMENT_ADJUSTED"))
    else if (vrn == "101747015")
      Ok(repaymentDetails3(now().minusDays(70).toString, "INITIAL", "CLAIM_QUERIED", "REPAYMENT_ADJUSTED"))
    else if (vrn == "101747016")
      Ok(repaymentDetails2DifferentPeriods(now().toString, now().minusDays(70).toString, "INITIAL", "REPAYMENT_ADJUSTED"))
    else if (vrn == "101747013")
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "ADJUSMENT_TO_TAX_DUE"))
    else if (vrn == "101748000")
      Ok(repaymentDetails3(now().toString, "INITIAL", "CLAIM_QUERIED", "REPAYMENT_SUSPENDED"))
    else if (vrn == "101747017")
      Ok(repaymentDetailSingleCompletedWithHash)
    else if (vrn.startsWith("1"))
      NotFound(repaymentDetailsNotFound)
    //return 4 completed
    else if (vrn.startsWith("3"))
      Ok(repaymentDetailsMultipleCompleted())
    //One completed, 3 inprogress
    else if (vrn.startsWith("4"))
      if (vrn.contains("P")) Ok(repaymentDetails3InProgress1Completed(true))
      else Ok(repaymentDetails3InProgress1Completed())
    //One completed
    else if (vrn.startsWith("5"))
      Ok(repaymentDetailSingleCompleted)
    //4 in progress
    else if (vrn.startsWith("9"))
      Ok(repaymentDetailsMultipleInProgress)
    else
      //successful within range
      Ok(repaymentDetailSingleInProgress)
  }
}
