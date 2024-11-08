/*
 * Copyright 2024 HM Revenue & Customs
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

import config.TaxYearConfig.totalIncomeIsEqualOrAboveThreshold
import controllers.journeys.income.routes
import models.common.AccountingType.{Accrual, Cash}
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.IndividualCategories
import pages.OneQuestionPage
import pages.expenses.tailoring.ExpensesCategoriesPage
import play.api.mvc.Call

trait IncomeBasePage[A] extends OneQuestionPage[A] {
  override def cyaPage(taxYear: TaxYear, businessId: BusinessId): Call =
    routes.IncomeCYAController.onPageLoad(taxYear, businessId)

  def redirectForAccountingType(userAnswers: UserAnswers, businessId: BusinessId, accrualRedirect: Call, cashRedirect: Call): Call =
    userAnswers.getAccountingType(businessId) match {
      case Accrual => accrualRedirect
      case Cash    => cashRedirect
    }

  def isTotalIncomeEqualOrAboveThreshold(userAnswers: UserAnswers, businessId: BusinessId): Boolean = {
    val nonTurnoverAmount: BigDecimal = userAnswers.get(NonTurnoverIncomeAmountPage, businessId).getOrElse(BigDecimal(0))
    val turnoverAmount: BigDecimal    = userAnswers.get(TurnoverIncomeAmountPage, businessId).getOrElse(BigDecimal(0))
    val expensesCategories: Boolean   = userAnswers.get(ExpensesCategoriesPage, businessId).exists(_ != IndividualCategories)
    val total                         = nonTurnoverAmount + turnoverAmount
    totalIncomeIsEqualOrAboveThreshold(total) && expensesCategories
  }
}
