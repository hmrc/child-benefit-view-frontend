/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package paymentsstubs.desvrt.model

import java.time.LocalDate.now

import play.api.libs.json.{JsValue, Json}

object DesData {

  //language=JSON
  def repaymentDetails2DifferentPeriods(date: String, date2: String, status1: String, status2: String): JsValue = Json.parse(
    s"""
        [
          {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AF",
            "riskingStatus": "$status1",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          },
          {
            "returnCreationDate": "$date2",
            "sentForRiskingDate": "$date2",
            "lastUpdateReceivedDate": "$date2",
            "periodKey": "18AG",
            "riskingStatus": "$status2",
            "vatToPay_BOX5": 1001,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          }
        ]
        """.stripMargin)

  //language=JSON
  def repaymentDetails1(date: String, status1: String): JsValue = Json.parse(
    s"""
        [
          {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AG",
            "riskingStatus": "$status1",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
        }
      ]
      """.stripMargin)

  //language=JSON
  def repaymentDetails2(date: String, status1: String, status2: String): JsValue = Json.parse(
    s"""
        [
          {
             "returnCreationDate": "$date",
             "sentForRiskingDate": "$date",
             "lastUpdateReceivedDate": "$date",
             "periodKey": "18AG",
             "riskingStatus": "$status1",
             "vatToPay_BOX5": 1000,
             "supplementDelayDays": 6,
             "originalPostingAmount": 5.56
         },
         {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AG",
            "riskingStatus": "$status2",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          }
        ]
        """.stripMargin)

  //language=JSON
  def repaymentDetails3(date: String, status1: String, status2: String, status3: String): JsValue = Json.parse(
    s"""
        [
          {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AG",
            "riskingStatus": "$status1",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          },
          {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AG",
            "riskingStatus": "$status2",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          },
          {
            "returnCreationDate": "$date",
            "sentForRiskingDate": "$date",
            "lastUpdateReceivedDate": "$date",
            "periodKey": "18AG",
            "riskingStatus": "$status3",
            "vatToPay_BOX5": 1000,
            "supplementDelayDays": 6,
            "originalPostingAmount": 5.56
          }
        ]
        """.stripMargin)

  //language=JSON
  def repaymentDetailSingleInProgress: JsValue = {
    val date = now().toString
    Json.parse(
      s"""
          [
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AG",
              "riskingStatus": "INITIAL",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 5.56
            }
          ]
          """.stripMargin)
  }

  //language=JSON
  def repaymentDetailSingleCompleted: JsValue = {
    val date = now().toString
    Json.parse(
      s"""
          [
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AG",
              "riskingStatus": "ADJUSTMENT_TO_TAX_DUE",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 5.56
            }
          ]
          """.stripMargin)
  }

  //language=JSON
  def repaymentDetailSingleCompletedWithHash: JsValue = {
    val date = now().toString
    Json.parse(
      s"""
          [
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "#001",
              "riskingStatus": "ADJUSTMENT_TO_TAX_DUE",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 5.56
            }
          ]
           """.stripMargin)
  }

  //language=JSON
  def repaymentDetailsCustom: JsValue = {
    val date = now().toString
    Json.parse(
      s"""
          [
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "19AI",
              "riskingStatus": "ADJUSMENT_TO_TAX_DUE",
              "vatToPay_BOX5": 4363089,
              "supplementDelayDays": 1,
              "originalPostingAmount": 52269.70
            }
          ]""".stripMargin)
  }

  //language=JSON
  def repaymentDetailsMultipleInProgress: JsValue = {
    val date = now().toString
    Json.parse(
      s"""
          [
            {
                "returnCreationDate": "$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AA",
                "riskingStatus": "INITIAL",
                "vatToPay_BOX5": 1001,
                "supplementDelayDays": 1,
                "originalPostingAmount": 796
            },
            {
                "returnCreationDate": "$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AD",
                "riskingStatus": "INITIAL",
                "vatToPay_BOX5": 1002,
                "supplementDelayDays": 1,
                "originalPostingAmount": 3.59
            },
            {
                "returnCreationDate": "$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AG",
                "riskingStatus": "SENT_FOR_RISKING",
                "vatToPay_BOX5": 1003,
                "supplementDelayDays": 1,
                "originalPostingAmount": 10169.45
            },
            {
                "returnCreationDate": "$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AJ",
                "riskingStatus": "CLAIM_QUERIED",
                "vatToPay_BOX5": 1004,
                "supplementDelayDays": 1,
                "originalPostingAmount": 796
            }
          ]
          """.stripMargin)
  }

  //language=JSON
  def repaymentDetailsMultipleCompleted(): JsValue = {
    val date = now().toString

    Json.parse(
      s"""
          [
            {
                "returnCreationDate": "$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AA",
                "riskingStatus": "REPAYMENT_ADJUSTED",
                "vatToPay_BOX5": 1001,
                "supplementDelayDays": 1,
                "originalPostingAmount": 796
            },
            {
                "returnCreationDate":"$date",
                "sentForRiskingDate": "$date",
                "lastUpdateReceivedDate": "$date",
                "periodKey": "18AD",
                "riskingStatus": "REPAYMENT_ADJUSTED",
                "vatToPay_BOX5": 1002,
                "supplementDelayDays": 1,
                "originalPostingAmount": 800
             },
             {
                 "returnCreationDate": "$date",
                 "sentForRiskingDate": "$date",
                 "lastUpdateReceivedDate": "$date",
                 "periodKey": "18AG",
                 "riskingStatus": "ADJUSMENT_TO_TAX_DUE",
                 "vatToPay_BOX5": 1003,
                 "supplementDelayDays": 1,
                 "originalPostingAmount": 900.45
             },
             {
                 "returnCreationDate": "$date",
                 "sentForRiskingDate": "$date",
                 "lastUpdateReceivedDate": "$date",
                 "periodKey": "18AJ",
                 "riskingStatus": "REPAYMENT_APPROVED",
                 "vatToPay_BOX5": 1004,
                 "supplementDelayDays": 1,
                 "originalPostingAmount": 796
             }
          ]
          """.stripMargin)
  }

  //language=JSON
  def repaymentDetails3InProgress1Completed(inPast: Boolean = false): JsValue = {
    val date = if (inPast) now().minusDays(50).toString else now.toString

    Json.parse(
      s"""
          [
            {
             "returnCreationDate": "$date",
             "sentForRiskingDate": "$date",
             "lastUpdateReceivedDate": "$date",
             "periodKey": "18AA",
             "riskingStatus": "SENT_FOR_RISKING",
             "vatToPay_BOX5": 1000,
             "supplementDelayDays": 1,
             "originalPostingAmount": 1100
            },
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AA",
              "riskingStatus": "REPAYMENT_APPROVED",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 1100
            },
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AD",
              "riskingStatus": "SENT_FOR_RISKING",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 1100
            },
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AG",
              "riskingStatus": "CLAIM_QUERIED",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 1100
            },
            {
              "returnCreationDate": "$date",
              "sentForRiskingDate": "$date",
              "lastUpdateReceivedDate": "$date",
              "periodKey": "18AJ",
              "riskingStatus": "CLAIM_QUERIED",
              "vatToPay_BOX5": 1000,
              "supplementDelayDays": 1,
              "originalPostingAmount": 1100
             }
          ]
      """.stripMargin)
  }

  //language=JSON
  val repaymentDetailsNotFound: JsValue = Json.parse(
    s"""
        {
           "code": "NOT_FOUND",
           "reason": "The remote endpoint has indicated that no data can be found"
         }
        """.stripMargin)

  // language=JSON
  val customerDataNotFound: JsValue = Json.parse(
    s"""
      {
         "code": "NOT_FOUND",
         "reason": "The back end has indicated that No subscription can be found."
      }
       """.stripMargin)

  def customerDataOk(isPartial: Boolean = false): JsValue = Json.parse(
    s"""
     {
         "approvedInformation": {
             "customerDetails": {
                 "nameIsReadOnly": true,
                 "organisationName": "TAXPAYER NAME_1",
                 "dataOrigin": "0001",
                 "mandationStatus": "1",
                 "registrationReason": "0001",
                 "effectiveRegistrationDate": "2017-01-02",
                 "businessStartDate": "2017-01-01",
                 "welshIndicator": true,
                 "partyType": "50",
                 "optionToTax": true,
                 "isPartialMigration": $isPartial,
                 "isInsolvent": false,
                 "overseasIndicator": true
             },
             "PPOB": {
                 "address": {
                     "line1": "VAT PPOB Line1",
                     "line2": "VAT PPOB Line2",
                     "line3": "VAT PPOB Line3",
                     "line4": "VAT PPOB Line4",
                     "postCode": "TF3 4ER",
                     "countryCode": "GB"
                 },
                 "contactDetails": {
                     "primaryPhoneNumber": "012345678901",
                     "mobileNumber": "012345678902",
                     "faxNumber": "012345678903",
                     "emailAddress": "lewis.hay@digital.hmrc.gov.uk",
                     "emailVerified": true
                 },
                 "websiteAddress": "www.tumbleweed.com"
             },
             "bankDetails": {
                 "accountHolderName": "Account holder",
                 "bankAccountNumber": "11112222",
                 "sortCode": "667788"
             },
             "businessActivities": {
                 "primaryMainCode": "10410",
                 "mainCode2": "10611",
                 "mainCode3": "10710",
                 "mainCode4": "10720"
             },
             "flatRateScheme": {
                 "FRSCategory": "003",
                 "FRSPercentage": 59.99,
                 "startDate": "0001-01-01",
                 "endDate": "9999-12-31",
                 "limitedCostTrader": true
             },
             "returnPeriod": {
                 "stdReturnPeriod": "MM"
             }
         },
         "inFlightInformation": {
             "changeIndicators": {
                 "organisationDetails": false,
                 "PPOBDetails": false,
                 "correspondenceContactDetails": false,
                 "bankDetails": true,
                 "returnPeriod": false,
                 "flatRateScheme": false,
                 "businessActivities": false,
                 "deregister": false,
                 "effectiveDateOfRegistration": false,
                 "mandationStatus": true
             },
             "inFlightChanges": {
                 "bankDetails": {
                     "formInformation": {
                         "formBundle": "092000001020",
                         "dateReceived": "2019-03-04"
                     },
                     "accountHolderName": "Account holder",
                     "bankAccountNumber": "11112222",
                     "sortCode": "667788"
                 },
                 "mandationStatus": {
                     "formInformation": {
                         "formBundle": "092000002124",
                         "dateReceived": "2019-08-15"
                     },
                     "mandationStatus": "3"
                 }
             }
         }
     }
       """.stripMargin)

  // language=JSON
  val customerDataOkNoBankDetails: JsValue = Json.parse(
    s"""
      {
        "approvedInformation": {
           "customerDetails": {
               "nameIsReadOnly": true,
               "organisationName": "TAXPAYER NAME_1",
               "dataOrigin": "0001",
               "mandationStatus": "1",
               "registrationReason": "0001",
               "effectiveRegistrationDate": "2017-01-02",
               "businessStartDate": "2017-01-01",
               "welshIndicator": true,
               "partyType": "50",
               "optionToTax": true,
               "isPartialMigration": false,
               "isInsolvent": false,
               "overseasIndicator": true
           },
           "PPOB": {
               "address": {
                   "line1": "VAT PPOB Line1",
                   "line2": "VAT PPOB Line2",
                   "line3": "VAT PPOB Line3",
                   "line4": "VAT PPOB Line4",
                   "postCode": "TF3 4ER",
                   "countryCode": "GB"
               },
               "contactDetails": {
                   "primaryPhoneNumber": "012345678901",
                   "mobileNumber": "012345678902",
                   "faxNumber": "012345678903",
                   "emailAddress": "lewis.hay@digital.hmrc.gov.uk",
                   "emailVerified": true
               },
               "websiteAddress": "www.tumbleweed.com"
           },
           "businessActivities": {
               "primaryMainCode": "10410",
               "mainCode2": "10611",
               "mainCode3": "10710",
               "mainCode4": "10720"
           },
           "flatRateScheme": {
               "FRSCategory": "003",
               "FRSPercentage": 59.99,
               "startDate": "0001-01-01",
               "endDate": "9999-12-31",
               "limitedCostTrader": true
           },
           "returnPeriod": {
               "stdReturnPeriod": "MM"
           }
       },
       "inFlightInformation": {
           "changeIndicators": {
               "organisationDetails": false,
               "PPOBDetails": false,
               "correspondenceContactDetails": false,
               "bankDetails": false,
               "returnPeriod": false,
               "flatRateScheme": false,
               "businessActivities": false,
               "deregister": false,
               "effectiveDateOfRegistration": false,
               "mandationStatus": true
           },
           "inFlightChanges": {
               "bankDetails": {
                   "formInformation": {
                       "formBundle": "092000001020",
                       "dateReceived": "2019-03-04"
                   },
                   "accountHolderName": "Account holder",
                   "bankAccountNumber": "11112222",
                   "sortCode": "667788"
               },
               "mandationStatus": {
                   "formInformation": {
                       "formBundle": "092000002124",
                       "dateReceived": "2019-08-15"
                   },
                   "mandationStatus": "3"
               }
           }
       }
    }
    """.stripMargin)

  // language=JSON
  val customerDataOkNoBankDetailsInFlight: JsValue = Json.parse(
    s"""
     {
         "approvedInformation": {
             "customerDetails": {
                 "nameIsReadOnly": true,
                 "organisationName": "TAXPAYER NAME_1",
                 "dataOrigin": "0001",
                 "mandationStatus": "1",
                 "registrationReason": "0001",
                 "effectiveRegistrationDate": "2017-01-02",
                 "businessStartDate": "2017-01-01",
                 "welshIndicator": true,
                 "partyType": "50",
                 "optionToTax": true,
                 "isPartialMigration": false,
                 "isInsolvent": false,
                 "overseasIndicator": true
             },
             "PPOB": {
                 "address": {
                     "line1": "VAT PPOB Line1",
                     "line2": "VAT PPOB Line2",
                     "line3": "VAT PPOB Line3",
                     "line4": "VAT PPOB Line4",
                     "postCode": "TF3 4ER",
                     "countryCode": "GB"
                 },
                 "contactDetails": {
                     "primaryPhoneNumber": "012345678901",
                     "mobileNumber": "012345678902",
                     "faxNumber": "012345678903",
                     "emailAddress": "lewis.hay@digital.hmrc.gov.uk",
                     "emailVerified": true
                 },
                 "websiteAddress": "www.tumbleweed.com"
             },
             "businessActivities": {
                 "primaryMainCode": "10410",
                 "mainCode2": "10611",
                 "mainCode3": "10710",
                 "mainCode4": "10720"
             },
             "flatRateScheme": {
                 "FRSCategory": "003",
                 "FRSPercentage": 59.99,
                 "startDate": "0001-01-01",
                 "endDate": "9999-12-31",
                 "limitedCostTrader": true
             },
             "returnPeriod": {
                 "stdReturnPeriod": "MM"
             }
         },
         "inFlightInformation": {
             "changeIndicators": {
                 "organisationDetails": false,
                 "PPOBDetails": false,
                 "correspondenceContactDetails": false,
                 "bankDetails": true,
                 "returnPeriod": false,
                 "flatRateScheme": false,
                 "businessActivities": false,
                 "deregister": false,
                 "effectiveDateOfRegistration": false,
                 "mandationStatus": true
             },
             "inFlightChanges": {
                 "bankDetails": {
                     "formInformation": {
                         "formBundle": "092000001020",
                         "dateReceived": "2019-03-04"
                     },
                     "accountHolderName": "Account holder",
                     "bankAccountNumber": "11112222",
                     "sortCode": "667788"
                 },
                 "mandationStatus": {
                     "formInformation": {
                         "formBundle": "092000002124",
                         "dateReceived": "2019-08-15"
                     },
                     "mandationStatus": "3"
                 }
             }
         }
     }
     """.stripMargin)

  // language=JSON
  val financialDataNotFound: JsValue = Json.parse(
    s"""
      {
        "code": "NOT_FOUND",
        "reason": "The remote endpoint has indicated that no data can be found."
      }
       """.stripMargin)

  // language=JSON
  val customerDataOkWithPartialBankDetails: JsValue = Json.parse(
    s"""
       {
         "approvedInformation": {
             "customerDetails": {
                 "nameIsReadOnly": true,
                 "organisationName": "TAXPAYER NAME_1",
                 "dataOrigin": "0001",
                 "mandationStatus": "1",
                 "registrationReason": "0001",
                 "effectiveRegistrationDate": "2017-01-02",
                 "businessStartDate": "2017-01-01",
                 "welshIndicator": true,
                 "partyType": "50",
                 "optionToTax": true,
                 "isPartialMigration": false,
                 "isInsolvent": false,
                 "overseasIndicator": true
             },
             "PPOB": {
                 "address": {
                     "line1": "VAT PPOB Line1",
                     "line2": "VAT PPOB Line2",
                     "line3": "VAT PPOB Line3",
                     "line4": "VAT PPOB Line4",
                     "postCode": "TF3 4ER",
                     "countryCode": "GB"
                 },
                 "contactDetails": {
                     "primaryPhoneNumber": "012345678901",
                     "mobileNumber": "012345678902",
                     "faxNumber": "012345678903",
                     "emailAddress": "lewis.hay@digital.hmrc.gov.uk",
                     "emailVerified": true
                 },
                 "websiteAddress": "www.tumbleweed.com"
             },
             "bankDetails": {
                 "bankAccountNumber": "11112222",
                 "sortCode": "667788"
             },
             "businessActivities": {
                 "primaryMainCode": "10410",
                 "mainCode2": "10611",
                 "mainCode3": "10710",
                 "mainCode4": "10720"
             },
             "flatRateScheme": {
                 "FRSCategory": "003",
                 "FRSPercentage": 59.99,
                 "startDate": "0001-01-01",
                 "endDate": "9999-12-31",
                 "limitedCostTrader": true
             },
             "returnPeriod": {
                 "stdReturnPeriod": "MM"
             }
         },
         "inFlightInformation": {
             "changeIndicators": {
                 "organisationDetails": false,
                 "PPOBDetails": false,
                 "correspondenceContactDetails": false,
                 "bankDetails": true,
                 "returnPeriod": false,
                 "flatRateScheme": false,
                 "businessActivities": false,
                 "deregister": false,
                 "effectiveDateOfRegistration": false,
                 "mandationStatus": true
             },
             "inFlightChanges": {
                 "bankDetails": {
                     "formInformation": {
                         "formBundle": "092000001020",
                         "dateReceived": "2019-03-04"
                     },
                     "accountHolderName": "Account holder",
                     "bankAccountNumber": "11112222",
                     "sortCode": "667788"
                 },
                 "mandationStatus": {
                     "formInformation": {
                         "formBundle": "092000002124",
                         "dateReceived": "2019-08-15"
                     },
                     "mandationStatus": "3"
                 }
             }
         }
       }
         """.stripMargin)

  // language=JSON
  def financialDataSingleCredit(vrn: String): JsValue = Json.parse(
    s"""
         {
           "idType": "VRN",
           "idNumber": "$vrn",
           "regimeType": "VATC",
           "processingDate": "2019-08-20T10:44:05Z",
           "financialTransactions": [
             {
               "chargeType": "VAT Return Credit Charge",
               "mainType": "VAT PA Default Interest",
               "periodKey": "18AG",
               "periodKeyDescription": "March 2018",
               "taxPeriodFrom": "2018-03-01",
               "taxPeriodTo": "2018-03-31",
               "businessPartner": "0100113120",
               "contractAccountCategory": "33",
               "contractAccount": "091700000405",
               "contractObjectType": "ZVAT",
               "contractObject": "00000180000000000165",
               "sapDocumentNumber": "003360001206",
               "sapDocumentNumberItem": "0002",
               "chargeReference": "XV002616013469",
               "mainTransaction": "4708",
               "subTransaction": "1175",
               "originalAmount": 5.56,
               "outstandingAmount": 5.56,
               "items": [
                 {
                   "subItem": "000",
                   "dueDate": "2018-08-24",
                   "amount": 5.56,
                   "clearingDate": "2018-03-01"
                 }
               ]
             }
           ]
       }
    """.stripMargin)

  //language=JSON
  def customFinancialDataSingleCredit(vrn: String): JsValue = Json.parse(
    s"""
      {
         "idType": "VRN",
         "idNumber": "$vrn",
         "regimeType": "VATC",
         "processingDate": "2019-08-20T10:44:05Z",
         "financialTransactions": [
           {
             "chargeType": "VAT Return Credit Charge",
             "mainType": "VAT PA Default Interest",
             "periodKey": "18AG",
             "periodKeyDescription": "March 2018",
             "taxPeriodFrom": "2019-09-31",
             "taxPeriodTo": "2019-09-31",
             "businessPartner": "0100113120",
             "contractAccountCategory": "33",
             "contractAccount": "091700000405",
             "contractObjectType": "ZVAT",
             "contractObject": "00000180000000000165",
             "sapDocumentNumber": "003360001206",
             "sapDocumentNumberItem": "0002",
             "chargeReference": "XV002616013469",
             "mainTransaction": "4708",
             "subTransaction": "1175",
             "originalAmount": 52269.70,
             "outstandingAmount": 52269.70,
             "items": [
               {
                 "subItem": "000",
                 "dueDate": "2018-08-24",
                 "amount": 5.56,
                 "clearingDate": "2018-03-01"
               }
             ]
           }
         ]
      }
  """.stripMargin)

  // language=JSON
  def financialDataSingleDebit(vrn: String): JsValue = Json.parse(
    s"""
         {
           "idType": "VRN",
           "idNumber": "$vrn",
           "regimeType": "VATC",
           "processingDate": "2019-08-20T10:44:05Z",
           "financialTransactions": [
             {
               "chargeType": "VAT Return Debit Charge",
               "mainType": "VAT PA Default Interest",
               "periodKey": "18AG",
               "periodKeyDescription": "March 2018",
               "taxPeriodFrom": "2018-03-01",
               "taxPeriodTo": "2018-03-31",
               "businessPartner": "0100113120",
               "contractAccountCategory": "33",
               "contractAccount": "091700000405",
               "contractObjectType": "ZVAT",
               "contractObject": "00000180000000000165",
               "sapDocumentNumber": "003360001206",
               "sapDocumentNumberItem": "0002",
               "chargeReference": "XV002616013469",
               "mainTransaction": "4708",
               "subTransaction": "1175",
               "originalAmount": 5.56,
               "outstandingAmount": 5.56,
               "items": [
                 {
                   "subItem": "000",
                   "dueDate": "2018-08-24",
                   "amount": 5.56,
                   "clearingDate": "2018-03-01"
                 }
               ]
             }
           ]
       }
       """.stripMargin)

  // language=JSON
  val ddOk: JsValue = Json.parse(
    s"""
       {
         "directDebitMandateFound": true,
         "directDebitDetails": [
           {
             "directDebitInstructionNumber": "091700000409",
             "directDebitPlanType": "VPP",
             "dateCreated": "2019-09-20",
             "accountHolderName": "Tester Surname",
             "sortCode": "404784",
             "accountNumber": "70872490"
           }
         ]
       }
       """.stripMargin)

  // language=JSON
  val ddOkNoMandate: JsValue = Json.parse(
    s"""
        {
          "directDebitMandateFound": false
        }
        """.stripMargin)

  // language=JSON
  val ddNotFound: JsValue = Json.parse(
    s"""
      {
        "code": "NOT_FOUND",
        "reason": "The back end has indicated that there is no match found for the given identifier"
      }
      """.stripMargin)
}
