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

import base.{CYAOnPageLoadControllerSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.income
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.Journey
import models.journeys.Journey.Income
import models.journeys.income.{IncomeJourneyAnswers, TradingAllowance}
import pages.income.IncomeCYAPage
import play.api.Application
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.income._
import views.html.journeys.income.IncomeCYAView

class IncomeCYAControllerSpec extends CYAOnPageLoadControllerSpec with CYAOnSubmitControllerBaseSpec[IncomeJourneyAnswers] {

  override val pageName: String = IncomeCYAPage.toString
  override val journey: Journey = Income
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

  override protected val userAnswers: UserAnswers = UserAnswers(userAnswersId, userAnswerData)

  override protected val journeyAnswers: IncomeJourneyAnswers = IncomeJourneyAnswers(
    incomeNotCountedAsTurnover = false,
    nonTurnoverIncomeAmount = None,
    turnoverIncomeAmount = 100.00,
    anyOtherIncome = false,
    otherIncomeAmount = None,
    turnoverNotTaxable = Some(false),
    notTaxableAmount = None,
    tradingAllowance = TradingAllowance.DeclareExpenses,
    howMuchTradingAllowance = None,
    tradingAllowanceAmount = None
  )

  def onPageLoadCall: (TaxYear, BusinessId) => Call = income.routes.IncomeCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = income.routes.IncomeCYAController.onSubmit

  def getSummaryList(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): SummaryList = SummaryList(
    rows = Seq(
      IncomeNotCountedAsTurnoverSummary.row(userAnswers, taxYear, userType.toString, businessId).value,
      TurnoverIncomeAmountSummary.row(userAnswers, taxYear, userType.toString, businessId).value,
      AnyOtherIncomeSummary.row(userAnswers, taxYear, userType.toString, businessId).value,
      TurnoverNotTaxableSummary.row(userAnswers, taxYear, userType.toString, businessId).value,
      TradingAllowanceSummary.row(userAnswers, taxYear, userType.toString, businessId).value
    ),
    classes = "govuk-!-margin-bottom-7"
  )

  override def createExpectedView(userType: UserType,
                                  summaryList: SummaryList,
                                  messages: Messages,
                                  application: Application,
                                  request: Request[_],
                                  pageInsetText: Option[String]): String = {
    val view = application.injector.instanceOf[IncomeCYAView]
    view(taxYear, businessId, summaryList, userType.toString)(request, messages).toString()
  }

  override val testDataCases: List[JsObject] =
    List(
      Json.obj(
        "incomeNotCountedAsTurnover" -> false,
        "turnoverIncomeAmount"       -> 100.00,
        "anyOtherIncome"             -> false,
        "turnoverNotTaxable"         -> false,
        "tradingAllowance"           -> "declareExpenses"
      )
    )

}
