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

package pages.expenses.tailoring

import models.common.BusinessId
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring._
import pages.expenses.tailoring.individualCategories.OfficeSuppliesPage
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import pages.{OneQuestionPage, QuestionPage}

case object ExpensesCategoriesPage extends OneQuestionPage[ExpensesTailoring] {
  override def toString: String = "expensesCategories"

  override def next(userAnswers: UserAnswers, businessId: BusinessId): Option[QuestionPage[_]] =
    userAnswers.get(this, businessId).flatMap {
      case TotalAmount          => Some(TotalExpensesPage)
      case IndividualCategories => Some(OfficeSuppliesPage)
      case NoExpenses           => None // CYA page, but it is a Page cannot return for QuestionPage
    }
}
