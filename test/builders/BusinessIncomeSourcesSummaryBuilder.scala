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

import base.SpecBase.businessId
import models.domain.BusinessIncomeSourcesSummary

object BusinessIncomeSourcesSummaryBuilder {

  val aBusinessIncomeSourcesSummary = BusinessIncomeSourcesSummary(
    businessId.value,
    100,
    100,
    100,
    0,
    Some(100),
    Some(100),
    Some(100),
    100,
    100
  )

  val aBusinessIncomeSourcesSummaryWithNetProfit = BusinessIncomeSourcesSummary(
    incomeSourceId = businessId.value,
    totalIncome = 100,
    totalExpenses = 100,
    netProfit = 100,
    netLoss = 0,
    totalAdditions = Some(200),
    totalDeductions = Some(0),
    accountingAdjustments = Some(100),
    taxableProfit = 100,
    taxableLoss = 100
  )

  val aBusinessIncomeSourcesSummaryWithNetLoss = BusinessIncomeSourcesSummary(
    incomeSourceId = businessId.value,
    totalIncome = 100,
    totalExpenses = 100,
    netProfit = 0,
    netLoss = 100,
    totalAdditions = Some(0),
    totalDeductions = Some(200),
    accountingAdjustments = Some(100),
    taxableProfit = 100,
    taxableLoss = 100
  )
}
