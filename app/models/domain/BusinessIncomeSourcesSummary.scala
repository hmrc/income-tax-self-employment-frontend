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

package models.domain

import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.libs.json.{Json, OFormat}

case class BusinessIncomeSourcesSummary(incomeSourceId: String,
                                        totalIncome: BigDecimal,
                                        totalExpenses: BigDecimal,
                                        netProfit: BigDecimal,
                                        netLoss: BigDecimal,
                                        totalAdditions: Option[BigDecimal],
                                        totalDeductions: Option[BigDecimal],
                                        accountingAdjustments: Option[BigDecimal],
                                        taxableProfit: BigDecimal,
                                        taxableLoss: BigDecimal) {

  def journeyIsNetProfitOrLoss: ProfitOrLoss = if (netLoss > 0) Loss else Profit

  def getNetBusinessProfitOrLossForTaxPurposes: BigDecimal =
    (if (journeyIsNetProfitOrLoss == Profit) netProfit else -netLoss) + totalAdditions.getOrElse(0) - totalDeductions.getOrElse(0)

  def getTaxableProfitOrLossAmount: BigDecimal = if (taxableLoss > 0) -taxableLoss else taxableProfit
}

object BusinessIncomeSourcesSummary {
  implicit val format: OFormat[BusinessIncomeSourcesSummary] = Json.format[BusinessIncomeSourcesSummary]

  val empty: BusinessIncomeSourcesSummary = BusinessIncomeSourcesSummary("", 0, 0, 0, 0, None, None, None, 0, 0)
}
