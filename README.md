
# income-tax-self-employment-frontend

This is where users can review and make changes to the Self-employment section of their income tax return.

## Running the service locally

you will need to have the following:
- Installed [MongoDB](https://docs.mongodb.com/manual/installation/)
- Installed/configured [service-manager](https://github.com/hmrc/service-manager)

The service manager profile for this service is:

    sm --start INCOME_TAX_SELF_EMPLOYMENT_FRONTEND

Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm --start INCOME_TAX_SUBMISSION_ALL -r

To run the service locally:

    sudo mongod (If not already running)
    sm --start INCOME_TAX_SUBMISSION_ALL -r
    sm --stop INCOME_TAX_SELF_EMPLOYMENT_FRONTEND
    ./run.sh **OR** sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run

This service runs on port: `localhost:10901`

### Creation of pages using G8-Scaffold

- All pages in this service are created using the HMRC G8 frontend scaffold.
- Documentation of the scaffold and how it is used can be found here:
    https://github.com/hmrc/hmrc-frontend-scaffold.g8/wiki/Usage

#### To add a new journey follow the below steps**

- go to root project
- call `sbt`
- choose which gitter template you are going to use, e.g. `g8Scaffold journeyBigDecimalPage`
    - the available list you can find in .g8 folder
- repeat the process until you generated all the pages
- at the end in root project call `./migrate.sh` to add necessary changes to app.routes and messages files
- call `sbt compile` - fix issues if any
- call `sbt test` - fix failing tests if any

#### Limitations

- Right now only templates with journey* require minimum rework, they compile. They just require few tests to add.
- Other templates are standard scaffold, and may require a lot of manual tasks to move the files to appropriate packages

#### Known Issues

- the current scaffold templates generate a lot of duplicated cost, we must improve our g8 template to stop that
- in the terminal, sometimes the cursor every few seconds is moved at the beginning. Keep typing, ignore that

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").