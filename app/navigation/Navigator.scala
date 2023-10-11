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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import pages._
import models._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => Int => UserAnswers => Call = {
    case CheckYourSelfEmploymentDetailsPage => taxYear => _ =>
      controllers.journeys.tradeDetails.routes.SelfEmploymentSummaryController.onPageLoad(taxYear)

    case SelfEmploymentSummaryPage => taxYear => _ =>
      controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, TradeDetails.toString, NormalMode)
    
    case SelfEmploymentAbroadPage => taxYear => _ =>
      controllers.journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear)

    case SelfEmploymentAbroadCYAPage => taxYear => _ =>
      controllers.journeys.routes.SectionCompletedStateController.onPageLoad(taxYear, Abroad.toString, NormalMode)

    case IncomeNotCountedAsTurnoverPage => taxYear => userAnswers =>
        if (userAnswers.get(IncomeNotCountedAsTurnoverPage).getOrElse(false)) {
          controllers.journeys.income.routes.NonTurnoverIncomeAmountController.onPageLoad(taxYear, NormalMode)
        } else {
          controllers.journeys.income.routes.TurnoverIncomeAmountController.onPageLoad(taxYear, NormalMode)
        }

    case NonTurnoverIncomeAmountPage => taxYear => _ => controllers.journeys.income.routes.TurnoverIncomeAmountController.onPageLoad(taxYear, NormalMode)

    case TurnoverIncomeAmountPage => taxYear => _ => controllers.journeys.income.routes.AnyOtherIncomeController.onPageLoad(taxYear, NormalMode)

    case AnyOtherIncomePage => taxYear => userAnswers =>
        if (userAnswers.get(AnyOtherIncomePage).getOrElse(false)) {
          controllers.journeys.income.routes.OtherIncomeAmountController.onPageLoad(taxYear, NormalMode)
        } else {
          controllers.journeys.income.routes.TurnoverNotTaxableController.onPageLoad(taxYear, NormalMode)
        }

//    case OtherIncomeAmountPage => taxYear => userAnswers =>
//      if (userAnswers.get(OtherIncomeAmountPage).contains("")) {
//      controllers.journeys.income.routes.TurnoverNotTaxableController.onPageLoad(taxYear, NormalMode)} //TODO Accrual or Cash basis

    case TurnoverNotTaxablePage => taxYear => userAnswers =>
        if (userAnswers.get(TurnoverNotTaxablePage).getOrElse(false)) {
          controllers.journeys.income.routes.NotTaxableAmountController.onPageLoad(taxYear, NormalMode)
        } else {
          controllers.journeys.income.routes.TradingAllowanceController.onPageLoad(taxYear, NormalMode)
        }

    case NotTaxableAmountPage => taxYear => _ => controllers.journeys.income.routes.TradingAllowanceController.onPageLoad(taxYear, NormalMode)

    case TradingAllowancePage => taxYear => _ => controllers.journeys.income.routes.HowMuchTradingAllowanceController.onPageLoad(taxYear, NormalMode)

    case HowMuchTradingAllowancePage => taxYear => _ => controllers.journeys.income.routes.TradingAllowanceAmountController.onPageLoad(taxYear, NormalMode)

    case TradingAllowanceAmountPage => taxYear => _ => controllers.journeys.income.routes.CheckYourIncomeController.onPageLoad(taxYear)

    //TODO check your income page

    case SectionCompletedStatePage => taxYear => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)

    case _ => taxYear => _ => controllers.journeys.routes.TaskListController.onPageLoad(taxYear)
  }

  private val checkRouteMap: Page => Int => UserAnswers => Call = {
    case SelfEmploymentAbroadPage => taxYear => _ =>
        controllers.journeys.abroad.routes.SelfEmploymentAbroadCYAController.onPageLoad(taxYear)

    case _ => taxYear => _ =>  controllers.standard.routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, taxYear: Int, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(taxYear)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(taxYear)(userAnswers)
  }
}
