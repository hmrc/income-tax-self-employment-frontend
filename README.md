# income-tax-self-employment-frontend
![](https://img.shields.io/github/v/release/hmrc/income-tax-self-employment-frontend)

The users can review and make changes to the Self-employment section of their income tax return.

## Running the service

you will need to have the following:
- Installed [MongoDB](https://docs.mongodb.com/manual/installation/)
- Installed/configured [service-manager](https://github.com/hmrc/service-manager)

The service manager profile for this service is:

    sm2 --start INCOME_TAX_SUBMISSION_ALL

To run the service locally:

    sm2 --stop INCOME_TAX_SELF_EMPLOYMENT_FRONTEND
    sbt run

The service runs on port `10901` by default.


## Auth Setup - How to enter the service

auth-wizard - http://localhost:9949/auth-login-stub/gg-sign-in

### Example Auth Setup - Individual

| FieldName           | Value                                                                |
|---------------------|----------------------------------------------------------------------|
| Redirect url        | http://localhost:9302/update-and-submit-income-tax-return/2025/start |
| Credential Strength | strong                                                               |
| Confidence Level     | 250                                                                  |
| Affinity Group       | Individual                                                           |
| Nino                | AA000001C                                                            |
| Enrolment Key 1     | HMRC-MTD-IT                                                          |
| Identifier Name 1    | MTDITID                                                              |
| Identifier Value 1   | 1234567890                                                           |

### Example Auth Setup - Agent
if running locally outside service manager ensure service is ran including testOnly Routes:

    sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes

| FieldName            | Value                                                                             |
|----------------------|-----------------------------------------------------------------------------------|
| Redirect url         | /test-only/2022/additional-parameters?ClientNino=AA123457A&ClientMTDID=1234567890 |
| Credential Strength  | weak                                                                              |
| Confidence Level      | 250                                                                               |
| Affinity Group        | Agent                                                                             |
| Enrolment Key 1      | HMRC-MTD-IT                                                                       |
| Identifier Name 1     | MTDITID                                                                           |
| Identifier Value 1    | 1234567890                                                                        |
| Enrolment Key 2      | HMRC-AS-AGENT                                                                     |
| Identifier Name 2     | AgentReferenceNumber                                                              |
| Identifier Value 2    | XARN1234567                                                                       |

## Creating pages

This project uses the [hmrc-frontend-scaffold.g8](https://github.com/hmrc/hmrc-frontend-scaffold.g8) template to generate frontend pages.

For instructions on how to create new pages using this scaffold, please refer to the [Usage Guide on the Wiki](https://github.com/hmrc/hmrc-frontend-scaffold.g8/wiki/Usage).

## Development

To build the project, run:

```
sbt clean compile
```
To run the tests, use:

```
sbt clean test it/test
```
To run code coverage

```
sbt clean coverage test it/test coverageReport
```
## Code Style
### Formatting code
This service uses [Scalafmt](https://scalameta.org/scalafmt/), a code formatter for Scala. The formatting rules configured for this repository are defined within [.scalafmt.conf](.scalafmt.conf). Prior to checking in any changes to this repository, please make sure all files are formatted correctly.

To apply formatting to this repository using the configured rules in [.scalafmt.conf](.scalafmt.conf) execute:

```
sbt scalafmtAll scalafmtSbt
```

To check files have been formatted as expected execute:

```
sbt scalafmtCheckAll scalafmtSbtCheck
```
## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").