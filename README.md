# child-benefit-data-stubs

Data stubs repository for child benefit service


# DES API's for Child benefit
 
Des API list implementaitons
##financical details
'http://localhost:10651/child-benefit/financial-details/AB654321'
 financical details 


http://localhost:10651/child-benefit/financial-details/YY123499D

# DES API's test data within this stub

Test data added with in conf/resources/des/api folder
Where each api ( i.e 1437, 1439) has a folder ( i.e conf/resources/des/api/1437 )

Test data taken from DES documentation and added into json files.

Child Benefit Service Dev and QA manages this test data.


# quick test 
run the stubs with in local machine
'sbt run '
and try to access following urls

/child-benefit/financial-details/:identifier
'
http://localhost:10651/child-benefit/financial-details/AB654321
http://localhost:10651/child-benefit/financial-details/YY123499D
'

/individuals/relationship/:idNumber
'
http://localhost:10651/individuals/relationship/96a2292e-5085-4bd7-b445-0c5f87353b71
http://localhost:10651/individuals/relationship/a1e8057e-fbbc-47a8-a8b4-78d9f015c252
http://localhost:10651/individuals/relationship/a1e8057e-fbbc-47a8-a8b4-78d9f015c253
http://localhost:10651/individuals/relationship/a1e8057e-fbbc-47a8-a8b4-78d9f015c255
http://localhost:10651/individuals/relationship/a1e8057e-fbbc-47a8-a8b4-78d9f015c254
'

/individuals/details/:idNumber/:resolveMerge
'
http://localhost:10651/individuals/details/AB049513/Y
http://localhost:10651/individuals/details/AB654321B/Y
'


# API#1437 Get Relationship Details
1. Success response - all fields, single relationship
   - conf/resources/des/api/individuals-relationship-details/a1e8057e-fbbc-47a8-a8b4-78d9f015c253.json
2. Success response - all fields, multiple relationship items, NINOs and TRNs
   - conf/resources/des/api/individuals-relationship-details/a1e8057e-fbbc-47a8-a8b4-78d9f015c252.json
3. Success response - no relationship entries
   - conf/resources/des/api/individuals-relationship-details/96a2292e-5085-4bd7-b445-0c5f87353b71.json
4. Error response - single code
   - conf/resources/des/api/individuals-relationship-details/a1e8057e-fbbc-47a8-a8b4-78d9f015c253.json
5. Error response - multiple codes
   - conf/resources/des/api/individuals-relationship-details/a1e8057e-fbbc-47a8-a8b4-78d9f015c253.json


  TODO - VERIFY ABOVE TEST DATA
