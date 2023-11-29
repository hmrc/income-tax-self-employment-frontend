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

package controllers.journeys.expenses.construction

import base.SpecBase
import models.NormalMode
import models.common.UserType.{Agent, Individual}
import models.database.UserAnswers
import models.journeys.Journey.ExpensesConstruction
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import viewmodels.checkAnswers.expenses.construction.ConstructionIndustryAmountSummary
import views.html.journeys.expenses.construction.ConstructionIndustryCYAView
import controllers.journeys
import controllers.journeys.expenses.construction

class ConstructionIndustryCYAControllerSpec extends SpecBase {

  private val userTypes = List(Individual, Agent)

  private val userAnswerData = Json.parse(s"""
       |{
       |  "$businessId": {
       |    "constructionAmount": 1235.4
       |  }
       |}
       |""".stripMargin)

  private val userAnswers = UserAnswers(userAnswersId, userAnswerData.as[JsObject])

  "ConstructionIndustryCYA Controller" - {

    userTypes.foreach { userType =>
      s".onPageLoad when user is an $userType should" - {
        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers), userType).build()

          implicit val appMessages: Messages = messages(application)

          running(application) {
            val view    = application.injector.instanceOf[ConstructionIndustryCYAView]
            val request = FakeRequest(GET, construction.routes.ConstructionIndustryCYAController.onPageLoad(taxYear, businessId).url)

            val expectedSummaryListRows = Seq(
              ConstructionIndustryAmountSummary.row(userAnswers, taxYear, businessId, userType)
            ).flatten
            val expectedSummaryLists = SummaryList(rows = expectedSummaryListRows, classes = "govuk-!-margin-bottom-7")
            val expectedNextRoute =
              journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, ExpensesConstruction.toString, NormalMode).url

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
