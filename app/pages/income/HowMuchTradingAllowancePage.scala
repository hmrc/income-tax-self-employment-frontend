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
import models.journeys.income.HowMuchTradingAllowance
import play.api.mvc.Call

case object HowMuchTradingAllowancePage extends IncomeBasePage[HowMuchTradingAllowance] {
  override def toString: String = "howMuchTradingAllowance"

  override def nextPageInNormalMode(userAnswers: UserAnswers, businessId: BusinessId, taxYear: TaxYear): Call =
    userAnswers.get(this, businessId) match {
      case Some(HowMuchTradingAllowance.LessThan) => routes.TradingAllowanceAmountController.onPageLoad(taxYear, businessId, NormalMode)
      case Some(HowMuchTradingAllowance.Maximum)  => routes.IncomeCYAController.onPageLoad(taxYear, businessId)
      case None                                   => standard.routes.JourneyRecoveryController.onPageLoad()
    }

  override def hasAllFurtherAnswers(businessId: BusinessId, userAnswers: UserAnswers): Boolean =
    userAnswers.get(this, businessId) match {
      case Some(HowMuchTradingAllowance.LessThan) => TradingAllowanceAmountPage.hasAllFurtherAnswers(businessId, userAnswers)
      case Some(HowMuchTradingAllowance.Maximum)  => true
      case None                                   => false
    }
}
