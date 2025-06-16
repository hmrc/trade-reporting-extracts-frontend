## Trade Reporting extracts front-end

This Trade Reporting and Extracts (TRE) will deliver a new service for traders to request reports containing customs declaration information for those that they have imported and/or exported. All Economic Operator Registration and Identification (EORI)-registered traders who have subscribed to CDS will have access to this service through a new Trader Reporting Dashboard on gov.uk. The Trader Reporting Dashboard front-end provides the ability to authorize a third party to access a trade reporting service so that third parties can request reports and download reports on their behalf.

### Run this service from your console using: 
`sbt run`

### Required services before running this project
`sm2 --start AUTH AUTH_LOGIN_API AUTH_LOGIN_STUB IDENTITY_VERIFICATION USER_DETAILS FEEDBACK_FRONTEND BAS_GATEWAY BAS_GATEWAY_FRONTEND BAS_STUBS`
* AUTH
* AUTH_LOGIN_API
* AUTH_LOGIN_STUB   
* IDENTITY_VERIFICATION
* USER_DETAILS
* FEEDBACK_FRONTEND
* BAS_GATEWAY
* BAS_GATEWAY_FRONTEND
* BAS_STUBS

### Use service manager to start up supporting services: 
`sm2 --start TRE_ALL`

### To apply formatting to your repository using the configured rules in .scalafmt.conf in a terminal execute:

`sbt scalafmtAll`

### To check files have been formatted as expected execute:

`sbt scalafmtCheckAll`