## trade-reporting-extracts-front-end

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)  [![Coverage](https://img.shields.io/badge/test_coverage-90-green.svg)](https://build.tax.service.gov.uk/job/TRE/job/trade-reporting-extracts-frontend/scoverage/) [![Accessibility](https://img.shields.io/badge/WCAG2.2-AA-purple.svg)](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

This frontend microservice powers the Trader Reporting Dashboard, enabling traders, declarants, and authorized third parties to request and download customs declaration reports. It is available to all EORI-registered users subscribed to CDS.

## Running the Service Locally

You can run the service locally in two ways:

### 1. Using sbt
After cloning the repository, run the following command in the root directory:

```
sbt run
```

### 2. Using Service Manager CLI
To use the service manager CLI (sm2), please refer to the [official setup guide](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html) for instructions on how to install and configure it locally.

Once set up:
- To start the service:
  ```sh
  sm2 --start TRADE_REPORTING_EXTRACTS_FRONEND
  ```
- To stop the service:
  ```sh
  sm2 --stop TRADE_REPORTING_EXTRACTS_FRONEND
  ```

### Required dependencies
This service is dependent on the following services:
- AUTH
- AUTH_LOGIN_API
- AUTH_LOGIN_STUB
- IDENTITY_VERIFICATION
- USER_DETAILS
- FEEDBACK_FRONTEND
- BAS_GATEWAY
- BAS_GATEWAY_FRONTEND
- BAS_STUBS

You can start all the dependent services at once using:
  ```sh
  sm2 --start TRE_ALL
  ```

## Login enrolments

The service's endpoints (that need Enrolment to access) can be accessed by using the enrolments below:

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-CUS-ORG  | EORINumber      | GB123456789012   |
| HMRC-CUS-ORG  | EORINumber      | GB123456789014   |

## Testing

The minimum requirement for test coverage is **90%**. Builds will fail when the project drops below this threshold.

| Command                                | Description                  |
|----------------------------------------|------------------------------|
| `sbt test`                             | Runs unit tests locally      |
| `sbt "test/testOnly *TEST_FILE_NAME*"` | Runs tests for a single file |

## Coverage

| Command                                             | Description                                                                                                        |
|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `sbt clean coverage test coverageReport`            | Generates a unit test coverage report. The report can be found at `target/scala-3.3.4/scoverage-report/index.html` |

## Feature Switches

The following feature switches are set in `application.conf`:

| Feature            | Description                                                        |
|--------------------|--------------------------------------------------------------------|
| welsh-translation  | Enables Welsh language translation support.                        |
| third-party        | Enables or disables all third-party related features (such as adding a third party or providing access to reports). When set to false, users cannot access any third-party functionality. |
| notifications      | Enables or disables all email notification features, such as adding additional emails to a report request. By default, users can only receive reports at the default company email associated with the EORI. |
| user-allow-list    | Restricts access to users in an allow list if set to true.         |

## Helpful Commands

| Command                                | Description                                                                                                 |
|----------------------------------------|-------------------------------------------------------------------------------------------------------------|
| sbt run                                | Runs the service locally                                                                                    |
| sbt clean                              | Cleans code                                                                                                 |
| sbt compile                            | Compiles the code                                                                                           |
| sbt coverage                           | Prints code coverage                                                                                        |
| sbt test                               | Runs unit tests                                                                                             |
| sbt it/test                            | Runs integration tests                                                                                      |
| sbt scalafmtCheckAll                   | Runs code formatting checks based on .scalafmt.conf                                                         |
| sbt scalafmtAll                        | Formats all the necessary files based on .scalafmt.conf                                                     |
| sbt "test/testOnly *TEST_FILE_NAME*"   | Runs tests for a single file                                                                                |
| sbt clean coverage test coverageReport | Generates a unit test coverage report that you can find here target/scala-3.3.5/scoverage-report/index.html |
