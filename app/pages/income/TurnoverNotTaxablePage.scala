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
import pages.redirectOnBoolean
import play.api.mvc.Call
import queries.Settable

case object TurnoverNotTaxablePage extends IncomeBasePage[Boolean] {
  override def toString: String = "turnoverNotTaxable"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call = redirectOnBoolean(
    this,
    userAnswers,
    businessId,
    routes.NotTaxableAmountController.onPageLoad(taxYear, businessId, NormalMode),
    routes.TradingAllowanceController.onPageLoad(taxYear, businessId, NormalMode)
  )

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers
      .get(this, businessId)
      .exists { a =>
        (a && NotTaxableAmountPage.hasAllFurtherAnswers(businessId, userAnswers)) ||
        (!a && TradingAllowancePage.hasAllFurtherAnswers(businessId, userAnswers))
      }

  override val dependentPagesWhenNo: List[Settable[_]] = List(NotTaxableAmountPage)
}
