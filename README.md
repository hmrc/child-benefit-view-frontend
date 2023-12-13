
# child-benefit-view-frontend

## OVERVIEW AND RESPONSIBILITY
child-benefit-view-frontend is a frontend service, that visualises entitlement, payment history and allows users to change their bank details and, when allowed, request a FTNAE extension to their claim.

child-benefit-view-frontend retrieves data from [child-benefit-service](https://github.com/hmrc/child-benefit-service) and visualises the data.
 
## CURRENT FEATURES AND ENDPOINTS FOR THE FRONT END SERVICE
The current journeys implemented and deployed are:

| Feature Name                             | Feature Description                                                                            | Primary Endpoint                                                                                                                                              |
|------------------------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Proof of Entitlement                     | for a child benefit user to view their current entitlements for their children                 | [{environment-host}:{environment-port}/child-benefit-service/view-proof-entitlement](https://staging.tax.service.gov.uk/child-benefit/view-proof-entitlement) |
| Payment History                          | for a child benefit user to view their payment history for all entitled children               | [{environment-host}:{environment-port}/child-benefit/view-payment-history](https://staging.tax.service.gov.uk/child-benefit/view-payment-history)             |
| Change of Bank                           | for a child benefit user to change the bank account they wish to have their benefits paid into | [{environment-host}:{environment-port}/child-benefit/change-bank/change-account](https://staging.tax.service.gov.uk/child-benefit/change-account)             |
| FTNAE (Full Time Non-Advanced Education) | for a child benefit user to update information about non-advanced education of their children  | [{environment-host}:{environment-port}/child-benefit/staying-in-education/extend-payments](https://staging.tax.service.gov.uk/child-benefit/extend-payments)  |

## HOW TO RUN THE SERVICE
To start the service locally, make use of Service Manager. The Service itself can be run using the CHILD_BENEFIT_VIEW_FRONTEND profile.

```> sm2 --start CHILD_BENEFIT_VIEW_FRONTEND```

The CHILD_BENEFIT_SERVICE_ALL profile will run all dependencies required to support this service, including the service itself.

```> sm2 --start CHILD_BENEFIT_SERVICE_ALL```

If you then wish to run child-benefit-view-frontend locally with changes, stop CHILD_BENEFIT_VIEW_FRONTEND in Service Manager and run the service in sbt

```> sm2 --stop CHILD_BENEFIT_VIEW_FRONTEND```

```.../child-benefit-view-frontend> sbt run```

## HOW TO TEST
The service itself has a suite of unit tests as a part of the repository. These are located under _./child_benefit_view_frontend/test_ can be run directly in your IDE or with a unit test running tool of your choice.

In addition, there is a suite of frontend journey tests in [child-benefit-ui-tests](https://github.com/hmrc/child-benefit-ui-tests) that will test this service in conjunction with [child-benefit-service](https://github.com/hmrc/child-benefit-service) and [child-benefit-data-stubs](https://github.com/hmrc/child-benefit-data-stubs).

## TEAM CHANNEL AND WHO OWNS THE SERVICE
Owning Team: SCA Optimization

Slack Channel: #team-sca-child-benefit

## LINKS TO KEY CONFLUENCE PAGES
| Description                               | Link                                                                                                         |
|-------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| SCA Child Benefit Space Home              | [[link]](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=312083264)                 |
| Feature Non-Specific Pages                | [[link]](https://confluence.tools.tax.service.gov.uk/display/SCAChB/All+Features+-+Generic+pages)            |
| Feature 1: View Payments and Entitlements | [[link]](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=504988536)                 |
| Feature 2: Change of Bank                 | [[link]](https://confluence.tools.tax.service.gov.uk/display/SCAChB/Feature+2%3A+Change+of+ChB+Bank+Account) |
| Feature 4: FTNAE                          | [[link]](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=524550686)                 |

### License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").