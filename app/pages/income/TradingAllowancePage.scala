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

package pages.income

import controllers.journeys.income.routes
import controllers.standard
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.income.TradingAllowance
import play.api.mvc.Call
import queries.Settable

case object TradingAllowancePage extends IncomeBasePage[TradingAllowance] {
  override def toString: String = "tradingAllowance"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = {
    val hasExistingExpensesOrCapitalAllowances = userAnswers.hasExistingExpensesOrCapitalAllowances(businessId)

    userAnswers.get(this, businessId) match {
      case Some(TradingAllowance.UseTradingAllowance) if hasExistingExpensesOrCapitalAllowances =>
        routes.TradingAllowanceWarningController.onPageLoad(taxYear, businessId)
      case Some(TradingAllowance.UseTradingAllowance) => routes.HowMuchTradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
      case Some(TradingAllowance.DeclareExpenses)     => routes.IncomeCYAController.onPageLoad(taxYear, businessId)
      case None                                       => standard.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId) match {
      case Some(TradingAllowance.UseTradingAllowance) => HowMuchTradingAllowancePage.hasAllFurtherAnswers(businessId, userAnswers)
      case Some(TradingAllowance.DeclareExpenses)     => true
      case None                                       => false
    }

  override val dependentPagesWhenAnswerChanges: List[Settable[_]] = List(HowMuchTradingAllowancePage, TradingAllowanceAmountPage)
}
