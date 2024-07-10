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

package pages.expenses.otherExpenses

import controllers.journeys.expenses.otherExpenses.routes
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OtherExpenses
import pages.OneQuestionPage
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.mvc.Call

case object OtherExpensesAmountPage extends OneQuestionPage[BigDecimal] {
  override def toString: String = "otherExpensesAmount"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    if (hasDisallowable(businessId, userAnswers)) routes.OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
    else routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined &&
      (!hasDisallowable(businessId, userAnswers) || OtherExpensesDisallowableAmountPage.hasAllFurtherAnswers(businessId, userAnswers))

  private def hasDisallowable(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(OtherExpensesPage, businessId).contains(OtherExpenses.YesDisallowable)
}
