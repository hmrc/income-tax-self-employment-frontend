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

  def expectedNetProfitLossSummaryList(profitOrLoss: ProfitOrLoss,
                                       turnover: BigDecimal,
                                       incomeNotCountedAsTurnover: BigDecimal,
                                       totalExpenses: BigDecimal,
                                       netProfitOrLoss: BigDecimal): String =
    s"""|SummaryListRow(Key(HtmlContent(profitOrLoss.turnover),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(incomeNotCountedAsTurnover.title),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(profitOrLoss.totalExpenses),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         totalExpenses)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfitOrLoss)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)""".stripMargin

  def expectedExpensesSummaryList(profitOrLoss: ProfitOrLoss,
                                  balancingCharge: BigDecimal,
                                  goodsAndServices: BigDecimal,
                                  disallowableExpenses: BigDecimal,
                                  totalAdditionsOrDeductions: BigDecimal): String = {
    val additionsDeductionCaption = if (profitOrLoss == Profit) { totalAdditionsCaption(profitOrLoss) }
    else { totalDeductionsCaption(profitOrLoss) }
    s"""|SummaryListRow(Key(HtmlContent(selectCapitalAllowances.balancingCharge),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         balancingCharge)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(goodsAndServicesForYourOwnUse.title.individual),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         goodsAndServices)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(profitOrLoss.disallowableExpenses),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         disallowableExpenses)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent($additionsDeductionCaption),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         totalAdditionsOrDeductions)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)""".stripMargin
  }

  def expectedCapitalAllowancesSummaryList(profitOrLoss: ProfitOrLoss,
                                           capitalAllowances: BigDecimal,
                                           turnoverNotTaxable: BigDecimal,
                                           totalDeductionsOrAdditions: BigDecimal): String = {
    val deductionsAdditionsCaption = if (profitOrLoss == Profit) { totalDeductionsCaption(profitOrLoss) }
    else { totalAdditionsCaption(profitOrLoss) }
    s"""|SummaryListRow(Key(HtmlContent(profitOrLoss.capitalAllowances),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         capitalAllowances)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(profitOrLoss.turnoverNotTaxable),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         turnoverNotTaxable)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent($deductionsAdditionsCaption),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         totalDeductionsOrAdditions)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)""".stripMargin
  }

  def expectedYourAdjustedProfitOrLossSummaryList(profitOrLoss: ProfitOrLoss,
                                                  netProfit: BigDecimal,
                                                  additionsAmount: BigDecimal,
                                                  deductionsAmount: BigDecimal,
                                                  netForTaxPurposes: BigDecimal,
                                                  adjustments: BigDecimal,
                                                  adjustedTaxableProfitForCurrentYear: BigDecimal): String = {
    val additionsRow =
      s"SummaryListRow(Key(HtmlContent(profitOrLoss.additions.$profitOrLoss),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(additionsAmount)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)"
    val deductionsRow =
      s"SummaryListRow(Key(HtmlContent(profitOrLoss.deductions.$profitOrLoss),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(deductionsAmount)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)"
    val netForTaxPurposesProfitOrLoss       = if (netForTaxPurposes < 0) Loss else Profit
    val adjustedTaxableAmountIsProfitOrLoss = if (adjustedTaxableProfitForCurrentYear < 0) Loss else Profit

    s"""|SummaryListRow(Key(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfit)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |${if (profitOrLoss == Profit) additionsRow else deductionsRow}
        |${if (profitOrLoss == Profit) deductionsRow
       else additionsRow}
        |SummaryListRow(Key(HtmlContent(profitOrLossCalculation.adjustedSummary.netForTaxPurposes.$netForTaxPurposesProfitOrLoss),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         netForTaxPurposes)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(journeys.adjustments),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(profitOrLossCalculation.adjustedSummary.adjustedTaxableProfitOrLoss.$adjustedTaxableAmountIsProfitOrLoss),govuk-!-font-weight-bold hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustedTaxableProfitForCurrentYear)}),govuk-!-font-weight-bold hmrc-summary-list__key govuk-!-text-align-right), ,None)""".stripMargin
  }

  def expectedAdjustmentsSummaryList(adjustments: BigDecimal): String =
    s"""|SummaryListRow(Key(HtmlContent(adjustments.anyOtherBusinessIncome),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)
        |SummaryListRow(Key(HtmlContent(adjustments.totalAdjustments),govuk-!-font-weight-regular hmrc-summary-list__key),Value(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),govuk-!-font-weight-regular hmrc-summary-list__key govuk-!-text-align-right), ,None)""".stripMargin

}
