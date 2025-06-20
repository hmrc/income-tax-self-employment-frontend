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

## Creating pages

This project uses the [hmrc-frontend-scaffold.g8](https://github.com/hmrc/hmrc-frontend-scaffold.g8) template to generate frontend pages.

For instructions on how to create new pages using this scaffold, please refer to the [Usage Guide on the Wiki](https://github.com/hmrc/hmrc-frontend-scaffold.g8/wiki/Usage).

### Formatting code
This library uses [Scalafmt](https://scalameta.org/scalafmt/), a code formatter for Scala. The formatting rules configured for this repository are defined within [.scalafmt.conf](.scalafmt.conf). Prior to checking in any changes to this repository, please make sure all files are formatted correctly.
- call `sbt it/test` - fix failing tests if any

To apply formatting to this repository using the configured rules in [.scalafmt.conf](.scalafmt.conf) execute:

```
sbt scalafmtAll
```

To check files have been formatted as expected execute:

```
sbt scalafmtCheckAll scalafmtSbtCheck
```

## Development

To format, build, and run code coverage, run: ./build.sh

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").