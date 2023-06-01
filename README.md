
# child-benefit-view-frontend

## OVERVIEW AND RESPONSIBILITY
child-benefit-view-frontend is a frontend service, that visualises entitlement and change of bank data retrieved from
child-benefit-service backend service. child-benefit-view-frontend retrieves data from child-benefit-service 
end visualises the data. The current journeys implemented and deployed are:
- Proof of entitlement - for a child benefit user to view their entitlements for their children
- Payment history - for a child benefit user to view their payment history for their children
- Change of bank - for a child benefit user to change the bank account they wish to be paid into
- FTNAE - for a child benefit user to update information about non-advanced education of their children
## RELEVANT ENDPOINTS FOR THE FRONT END SERVICE 

- https://www.development.tax.service.gov.uk/child-benefit-service/view-proof-entitlement
- https://www.development.tax.service.gov.uk/child-benefit/view-payment-history
- https://www.development.tax.service.gov.uk/child-benefit/change-bank/change-account
- https://www.development.tax.service.gov.uk/child-benefit/staying-in-education/extend-payments
## HOW TO TEST
sm --start CHILD-BENEFIT-SERVICE-ALL

(Having signed in to the Government Gateway on the related environment)
- <environment-host:environment-port>/child-benefit-service/view-proof-entitlement
- <environment-host:environment-port>/child-benefit/view-payment-history
- <environment-host:environment-port>/child-benefit/change-bank/change-account
- <environment-host:environment-port>/child-benefit/staying-in-education/extend-payments
eg: 

- https://www.development.tax.service.gov.uk/child-benefit-service/view-proof-entitlement
- https://www.development.tax.service.gov.uk/child-benefit/view-payment-history
- https://www.development.tax.service.gov.uk/child-benefit/change-bank/change-account
- https://www.development.tax.service.gov.uk/child-benefit/staying-in-education/extend-payments
## TEAM CHANNEL AND WHO OWNS THE SERVICE
\#team-sca-child-benefit
## LINK TO CONFLUENCE PAGE
https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=504988536

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
