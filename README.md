# trade-reporting-extracts-frontend

## About

This frontend microservice powers the Trader Reporting Dashboard, enabling traders, declarants, and authorized third parties to request and download customs declaration reports. It is available to all EORI-registered users subscribed to CDS.

## How to Run this Service

To run the service locally, you can use:

```bash
 sbt run
```

### Prerequisites

To use the service manager CLI (sm2), please refer to the [official setup guide](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html) for instructions on how to install and configure it locally.

Once set up:
- To start this specific use:
  ```sh
  sm2 --start TRADE_REPORTING_EXTRACTS_FRONTEND
  ```
- To run the full set of services connected to this frontend, use:
  ```sh
  sm2 --start TRE_ALL
  ```
- To stop the service:
  ```sh
  sm2 --stop TRADE_REPORTING_EXTRACTS_FRONTEND
  ```

## How to Test this Service

The minimum requirement for test coverage is **90%**. Builds will fail when the project drops below this threshold.

| Command                                  | Description                                                                                                        |
|------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `sbt test`                               | Runs unit tests locally                                                                                            |
| `sbt "test/testOnly *TEST_FILE_NAME*"`   | Runs tests for a single file                                                                                       |
| `sbt it/test`                            | Runs integration tests                                                                                             |
| `sbt clean coverage test coverageReport` | Generates a unit test coverage report. The report can be found at `target/scala-3.3.4/scoverage-report/index.html` |
| `sbt testAndCoverage`                    | Runs tests and generates a coverage report                                                                         |  
| `sbt prePR`                              | Run scala formating, test and coverage. Designed to be ran before raising a PR.                                    |
| `sbt preMerge`                           | Run scala check all, test and coverage. Designed to be ran before merging.                                         |

### UI-Journey Tests
The Trade-reporting-extracts-ui-journey-tests are ran in the pipeline and can be run against the frontend. For more info see the readme:
- [trade-reporting-extracts-ui-journey-tests](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/TRE/job/trade-reporting-extracts-ui-journey-tests/)

## Manual Testing in QA and Staging
Once the above acceptance tests jobs have been completed successfully, manually verify the service in the QA and Staging environments.
> **Note:** The QA environment uses real upstream services, so the acceptance tests are **not** run against QA. QA must be verified manually.
> Before testing in QA, make sure the EORINumber being used has a valid CDS subscription.

| Environment | Service URL                                                                                      | 
|-------------|--------------------------------------------------------------------------------------------------|
| Local       | [Open the service in Local](http://localhost:9949/auth-login-stub/gg-sign-in)                    |
| QA          | [Open the service in QA](https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in)           |
| Staging     | [Open the service in Staging](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in) |

## Service Catalogue
- You can find this repo in the MDTP catalogue here: [trade-reporting-extracts-frontend](https://catalogue.tax.service.gov.uk/repositories/trade-reporting-extracts-frontend)

## Jenkins Pipeline
- [trade-reporting-extracts-frontend](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/TRE/job/trade-reporting-extracts-frontend/)
- [trade-reporting-extracts-frontend-pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/TRE/job/trade-reporting-extracts-frontend-pipeline/)
- [trade-reporting-extracts-frontend-pr-builder](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/TRE/job/trade-reporting-extracts-frontend-pr-builder/)

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).
Depending on what you wish to run, you can use one of these profiles (which include the trade-reporting-extracts-frontend service):

## Enrolment Required

The service's endpoints (that need Enrolment to access) can be accessed by using the enrolments below:

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-CUS-ORG  | EORINumber      | GB123456789012   |
| HMRC-CUS-ORG  | EORINumber      | GB123456789014   |

sm2 --start TRE_ALL

### Developer Notes
http://localhost:2102/request-customs-declaration-data

### Scalafmt

The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.
To check all project files are formatted as expected use the following commands:

```bash
 sbt scalafmtCheckAll scalafmtCheck
```

Format `*.sbt` and `project/*.scala` files as follows:

```bash
 sbt scalafmtSbt
```

Format all project files as follows:

```bash
 sbt scalafmtAll
```

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This code is open source software licensed under
the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).****
