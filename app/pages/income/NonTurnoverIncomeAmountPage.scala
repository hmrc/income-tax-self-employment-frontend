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
import models.NormalMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import play.api.mvc.Call

case object NonTurnoverIncomeAmountPage extends IncomeBasePage[BigDecimal] {
  override def toString: String = "nonTurnoverIncomeAmount"

  override def cyaPage(userAnswers: UserAnswers, taxYear: TaxYear, businessId: BusinessId): Call =
    if (isTotalIncomeEqualOrAboveThreshold(userAnswers, businessId))
      routes.IncomeExpensesWarningController.onPageLoad(taxYear, businessId)
    else
      super.cyaPage(taxYear, businessId)

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    routes.TurnoverIncomeAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId).isDefined && TurnoverIncomeAmountPage.hasAllFurtherAnswers(businessId, userAnswers)
}
