### Trade Reporting extracts front-end

### Required services before running this project
sm2 --start MONGO AUTH AUTH_LOGIN_API AUTH_LOGIN_STUB IDENTITY_VERIFICATION USER_DETAILS
* MONGO  
* AUTH
* AUTH_LOGIN_API
* AUTH_LOGIN_STUB   
* IDENTITY_VERIFICATION
* USER_DETAILS

### To apply formatting to your repository using the configured rules in .scalafmt.conf in a terminal execute:

sbt scalafmtAll

### To check files have been formatted as expected execute:

sbt scalafmtCheckAll