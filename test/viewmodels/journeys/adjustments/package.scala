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

package viewmodels.journeys

import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import utils.MoneyUtils.formatPosNegMoneyWithPounds
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{totalAdditionsCaption, totalDeductionsCaption}

package object adjustments {

  def expectedNetProfitLossTable(profitOrLoss: ProfitOrLoss,
                                 turnover: BigDecimal,
                                 incomeNotCountedAsTurnover: BigDecimal,
                                 totalExpenses: BigDecimal,
                                 netProfitOrLoss: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(profitOrLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(incomeNotCountedAsTurnover.title),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfitOrLoss)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

  def expectedExpensesTable(profitOrLoss: ProfitOrLoss,
                            balancingCharge: BigDecimal,
                            goodsAndServices: BigDecimal,
                            disallowableExpenses: BigDecimal,
                            totalAdditionsOrDeductions: BigDecimal): String = {
    val additionsDeductionCaption = if (profitOrLoss == Profit) { totalAdditionsCaption(profitOrLoss) }
    else { totalDeductionsCaption(profitOrLoss) }
    s"""|List(TableRow(HtmlContent(selectCapitalAllowances.balancingCharge),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         balancingCharge)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(goodsAndServicesForYourOwnUse.title.individual),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         goodsAndServices)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.disallowableExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         disallowableExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent($additionsDeductionCaption),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalAdditionsOrDeductions)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin
  }

  def expectedCapitalAllowancesTable(profitOrLoss: ProfitOrLoss,
                                     capitalAllowances: BigDecimal,
                                     turnoverNotTaxable: BigDecimal,
                                     totalDeductionsOrAdditions: BigDecimal): String = {
    val deductionsAdditionsCaption = if (profitOrLoss == Profit) { totalDeductionsCaption(profitOrLoss) }
    else { totalAdditionsCaption(profitOrLoss) }
    s"""|List(TableRow(HtmlContent(profitOrLoss.capitalAllowances),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         capitalAllowances)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.turnoverNotTaxable),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnoverNotTaxable)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent($deductionsAdditionsCaption),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalDeductionsOrAdditions)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin
  }

  def expectedYourAdjustedProfitOrLossTable(profitOrLoss: ProfitOrLoss,
                                            netProfit: BigDecimal,
                                            additionsAmount: BigDecimal,
                                            deductionsAmount: BigDecimal,
                                            netForTaxPurposes: BigDecimal,
                                            adjustments: BigDecimal,
                                            adjustedTaxableProfitForCurrentYear: BigDecimal): String = {
    val additionsRow =
      s"List(TableRow(HtmlContent(profitOrLoss.additions.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(additionsAmount)}),None,govuk-!-text-align-right ,None,None,Map()))"
    val deductionsRow =
      s"List(TableRow(HtmlContent(profitOrLoss.deductions.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(deductionsAmount)}),None,govuk-!-text-align-right ,None,None,Map()))"
    val netForTaxPurposesProfitOrLoss = if (netForTaxPurposes < 0) Loss else Profit

    s"""|List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfit)}),None,govuk-!-text-align-right ,None,None,Map()))
        |${if (profitOrLoss == Profit) additionsRow else deductionsRow}
        |${if (profitOrLoss == Profit) deductionsRow
       else additionsRow}
        |List(TableRow(HtmlContent(profitOrLossCalculation.adjustedSummary.netForTaxPurposes.$netForTaxPurposesProfitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netForTaxPurposes)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(journeys.adjustments),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLossCalculation.adjustedSummary.adjustedTaxableProfitOrLoss.$profitOrLoss),None,govuk-!-font-weight-bold,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustedTaxableProfitForCurrentYear)}),None,govuk-!-text-align-right govuk-!-font-weight-bold,None,None,Map()))""".stripMargin
  }

  def expectedAdjustmentsTable(adjustments: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(adjustments.anyOtherBusinessIncome),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(adjustments.totalAdjustments),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

}
