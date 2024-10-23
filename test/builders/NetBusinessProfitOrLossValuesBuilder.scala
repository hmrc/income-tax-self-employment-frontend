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

package builders

import models.journeys.adjustments.NetBusinessProfitOrLossValues

object NetBusinessProfitOrLossValuesBuilder {
  val aNetBusinessProfitValues: NetBusinessProfitOrLossValues =
    NetBusinessProfitOrLossValues(
      turnover = 100,
      incomeNotCountedAsTurnover = 0,
      totalExpenses = 100,
      netProfit = 100,
      netLoss = 0,
      balancingCharge = 100,
      goodsAndServicesForOwnUse = 100,
      disallowableExpenses = 100,
      totalAdditions = 200,
      capitalAllowances = 100,
      turnoverNotTaxableAsBusinessProfit = 0,
      totalDeductions = 0,
      outstandingBusinessIncome = 500
    )
  val aNetBusinessLossValues: NetBusinessProfitOrLossValues =
    NetBusinessProfitOrLossValues(
      turnover = 100,
      incomeNotCountedAsTurnover = 0,
      totalExpenses = 100,
      netProfit = 0,
      netLoss = 100,
      balancingCharge = 100,
      goodsAndServicesForOwnUse = 100,
      disallowableExpenses = 100,
      totalAdditions = 0,
      capitalAllowances = 100,
      turnoverNotTaxableAsBusinessProfit = 0,
      totalDeductions = 200,
      outstandingBusinessIncome = 500
    )
}
