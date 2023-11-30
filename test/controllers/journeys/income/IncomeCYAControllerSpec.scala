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

import base.{CYAControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import models.common.UserType
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.Income
import models.journeys.income.IncomeJourneyAnswers
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.income._
import views.html.journeys.income.IncomeCYAView

class IncomeCYAControllerSpec extends CYAControllerBaseSpec with CYAOnSubmitControllerBaseSpec[IncomeJourneyAnswers] {

  private val userAnswerData = Json
    .parse(s"""
         |{
         |  "$businessId": {
         |    "incomeNotCountedAsTurnover": false,
         |    "turnoverIncomeAmount": 100.00,
         |    "anyOtherIncome": false,
         |    "turnoverNotTaxable": false,
         |    "tradingAllowance": "declareExpenses"
         |  }
         |}
         |""".stripMargin)
    .as[JsObject]

  override val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)
  override val journey: Journey         = Income

  override lazy val onPageLoadRoute: String = routes.IncomeCYAController.onPageLoad(taxYear, businessId).url
  override lazy val onSubmitRoute: String   = routes.IncomeCYAController.onSubmit(taxYear, businessId).url

  override def expectedSummaryList(user: UserType)(implicit messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      IncomeNotCountedAsTurnoverSummary.row(userAnswers, taxYear, user.toString, businessId).value,
      TurnoverIncomeAmountSummary.row(userAnswers, taxYear, user.toString, businessId).value,
      AnyOtherIncomeSummary.row(userAnswers, taxYear, user.toString, businessId).value,
      TurnoverNotTaxableSummary.row(userAnswers, taxYear, user.toString, businessId).value,
      TradingAllowanceSummary.row(userAnswers, taxYear, user.toString, businessId).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def expectedView(scenario: TestScenario, summaryList: SummaryList, nextRoute: String)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[IncomeCYAView]
    view(taxYear, businessId, summaryList, scenario.userType.toString)(request, messages).toString()
  }

}
