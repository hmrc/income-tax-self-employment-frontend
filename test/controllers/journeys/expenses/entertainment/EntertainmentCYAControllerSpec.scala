/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.journeys.expenses.entertainment

import base.SpecBase
import controllers.journeys.expenses.entertainment.routes.EntertainmentCYAController
import controllers.journeys.routes.SectionCompletedStateController
import models.NormalMode
import models.database.UserAnswers
import models.journeys.Journey.ExpensesEntertainment
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.entertainment.EntertainmentAmountSummary
import views.html.journeys.expenses.entertainment.EntertainmentCYAView

class EntertainmentCYAControllerSpec extends SpecBase {

  private val userTypes = List(individual, agent)

  private val userAnswerData = Json.parse(s"""
       |{
       |  "$stubbedBusinessId": {
       |    "entertainmentAmount": 1235.4
       |  }
       |}
       |""".stripMargin)

  private val userAnswers = UserAnswers(userAnswersId, userAnswerData.as[JsObject])

  "EntertainmentCYAController Controller" - {

    userTypes.foreach { userType =>
      s".onPageLoad when user is an $userType should" - {
        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent(userType)).build()

          implicit val appMessages: Messages = messages(application)

          running(application) {
            val view    = application.injector.instanceOf[EntertainmentCYAView]
            val request = FakeRequest(GET, EntertainmentCYAController.onPageLoad(taxYear, stubbedBusinessId).url)

            val expectedSummaryListRows = Seq(
              EntertainmentAmountSummary.row(userAnswers, taxYear, stubbedBusinessId, userType)
            ).flatten
            val expectedSummaryLists = SummaryList(rows = expectedSummaryListRows, classes = "govuk-!-margin-bottom-7")
            val expectedNextRoute =
              SectionCompletedStateController.onPageLoad(taxYear, stubbedBusinessId, ExpensesEntertainment.toString, NormalMode).url

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(taxYear, userType, expectedSummaryLists, expectedNextRoute)(
              request,
              messages(application)).toString
          }
        }
      }
    }
  }

}
