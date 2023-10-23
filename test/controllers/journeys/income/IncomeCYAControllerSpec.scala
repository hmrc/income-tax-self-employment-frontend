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

package controllers.journeys.income

import base.SpecBase
import controllers.journeys.income.routes.IncomeCYAController
import models.UserAnswers
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.income._
import views.html.journeys.income.IncomeCYAView

import scala.concurrent.Future

class IncomeCYAControllerSpec extends SpecBase {

  private val authUserType = "individual"

  private val userAnswerData = Json.parse(s"""
       |{
       |  "incomeNotCountedAsTurnover": false,
       |  "turnoverIncomeAmount": 1234.55,
       |  "anyOtherIncome": false,
       |  "turnoverNotTaxable": false,
       |  "tradingAllowance": "declareExpenses"
       |}
       |""".stripMargin)

  private val userAnswers = UserAnswers("some_id", userAnswerData.as[JsObject])

  private lazy val nextRoute = controllers.standard.routes.JourneyRecoveryController.onPageLoad(None).url

  "IncomeCYAController" - {
    "when handling a request to load a page" - {
      "must return a 200 OK, passing in the view as the content" in {
        val application                    = applicationBuilder(userAnswers = Some(userAnswers)).build()
        implicit val appMessages: Messages = messages(application)

        running(application) {
          val view    = application.injector.instanceOf[IncomeCYAView]
          val request = FakeRequest(GET, IncomeCYAController.onPageLoad(taxYear).url)

          val expectedSummaryListRows = Seq(
            AnyOtherIncomeSummary.row(userAnswers, taxYear, authUserType),
            IncomeNotCountedAsTurnoverSummary.row(userAnswers, taxYear, authUserType),
            TradingAllowanceSummary.row(userAnswers, taxYear, authUserType),
            TurnoverIncomeAmountSummary.row(userAnswers, taxYear, authUserType),
            TurnoverNotTaxableSummary.row(userAnswers, taxYear, authUserType)
          ).flatten

          val expectedSummaryLists = SummaryList(rows = expectedSummaryListRows, classes = "govuk-!-margin-bottom-7")

          val result: Future[Result] = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(taxYear, expectedSummaryLists, nextRoute, authUserType)(request, messages(application)).toString
        }
      }
    }
  }

}
