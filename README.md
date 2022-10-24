
# child-benefit-entitlement-frontend

## Overview and responsibility
child-benefit-entitlement-frontend is a frontend service, that visualises entitlement data retrieved from
child-benefit-service backend service. child-benefit-entitlement-frontend retrieves data from child-benefit-service 
end visualises the data in 2 different main pages
- Proof of entitlement
- Payment history

## Relevant endpoints for frontend service 

https://www.development.tax.service.gov.uk/child-benefit-service/view-proof-entitlement
https://www.development.tax.service.gov.uk/child-benefit/view-payment-history

## Documentation of how to test
sm --start CHILD-BENEFIT-SERVICE-ALL

(Having signed in to the Government Gateway on the related environment)
<environment-host:environment-port>/child-benefit-service/view-proof-entitlement
<environment-host:environment-port>/child-benefit/view-payment-history
eg: 

https://www.development.tax.service.gov.uk/child-benefit-service/view-proof-entitlement
https://www.development.tax.service.gov.uk/child-benefit/view-payment-history
## Team channel and who owns the service
\#int-sca-childbenefitdigital
## Link to confluence page
https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=504988536

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
