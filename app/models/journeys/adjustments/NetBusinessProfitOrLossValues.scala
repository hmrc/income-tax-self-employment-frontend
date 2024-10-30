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

package models.journeys.adjustments

import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.libs.json.{Format, Json}

case class NetBusinessProfitOrLossValues(turnover: BigDecimal,
                                         incomeNotCountedAsTurnover: BigDecimal,
                                         totalExpenses: BigDecimal,
                                         netProfit: BigDecimal,
                                         netLoss: BigDecimal,
                                         balancingCharge: BigDecimal,
                                         goodsAndServicesForOwnUse: BigDecimal,
                                         disallowableExpenses: BigDecimal,
                                         totalAdditions: BigDecimal,
                                         capitalAllowances: BigDecimal,
                                         turnoverNotTaxableAsBusinessProfit: BigDecimal,
                                         totalDeductions: BigDecimal,
                                         outstandingBusinessIncome: BigDecimal) {

  val netProfitOrLoss: ProfitOrLoss     = if (netLoss > 0) Loss else Profit
  val netProfitOrLossAmount: BigDecimal = if (netProfitOrLoss == Profit) netProfit else netLoss

  def getNetBusinessProfitOrLossForTaxPurposes: BigDecimal =
    if (netProfitOrLoss == Profit) netProfit + totalAdditions - totalDeductions else netLoss - totalAdditions + totalDeductions
}

object NetBusinessProfitOrLossValues {
  implicit val formats: Format[NetBusinessProfitOrLossValues] = Json.format[NetBusinessProfitOrLossValues]
}
