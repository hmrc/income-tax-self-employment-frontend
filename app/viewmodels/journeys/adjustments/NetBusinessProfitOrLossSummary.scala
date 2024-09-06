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

package viewmodels.journeys.adjustments

import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

case class NetBusinessProfitOrLossSummary(netProfitLossTable: Table, expensesTable: Table, capitalAllowancesTable: Table)

object NetBusinessProfitOrLossSummary {

  def additionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOrLoss.additions.$profitOrLoss"
  def deductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOrLoss.deductions.$profitOrLoss"

  def buildTables(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues)(implicit messages: Messages): NetBusinessProfitOrLossSummary =
    NetBusinessProfitOrLossSummary(
      buildNetProfitLossTable(
        Profit,
        netBusinessProfitOrLossValues.turnover,
        netBusinessProfitOrLossValues.incomeNotCountedAsTurnover,
        netBusinessProfitOrLossValues.totalExpenses,
        netBusinessProfitOrLossValues.netProfit
      ),
      buildExpensesTable(
        Profit,
        netBusinessProfitOrLossValues.balancingCharge,
        netBusinessProfitOrLossValues.goodsAndServicesForOwnUse,
        netBusinessProfitOrLossValues.disallowableExpenses,
        netBusinessProfitOrLossValues.totalAdditionsToNetProfit
      ),
      buildCapitalAllowancesTable(
        Profit,
        netBusinessProfitOrLossValues.capitalAllowances,
        netBusinessProfitOrLossValues.turnoverNotTaxableAsBusinessProfit,
        netBusinessProfitOrLossValues.totalDeductionsFromNetProfit
      )
    )

  def buildNetProfitLossTable(profitOrLoss: ProfitOrLoss,
                              turnover: BigDecimal,
                              incomeNotCountedAsTurnover: BigDecimal,
                              totalExpenses: BigDecimal,
                              netProfitOrLoss: BigDecimal)(implicit messages: Messages): Table = {

    val netProfitOrLossRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.turnover", turnover),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", incomeNotCountedAsTurnover),
      buildTableAmountRow("profitOrLoss.totalExpenses", totalExpenses),
      buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", netProfitOrLoss)
    )

    buildTable(
      None,
      netProfitOrLossRows,
      caption = Some(messages(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildExpensesTable(profitOrLoss: ProfitOrLoss,
                         balancingCharge: BigDecimal,
                         goodsAndServices: BigDecimal,
                         disallowableExpenses: BigDecimal,
                         totalAdditions: BigDecimal)(implicit messages: Messages): Table = {

    val additionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", balancingCharge),
      buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", goodsAndServices),
      buildTableAmountRow("profitOrLoss.disallowableExpenses", disallowableExpenses),
      buildTableAmountRow(
        if (profitOrLoss == Profit) { additionsCaption(profitOrLoss) }
        else { deductionsCaption(profitOrLoss) },
        totalAdditions)
    )

    buildTable(
      None,
      additionsRows,
      caption = Some(messages(if (profitOrLoss == Profit) additionsCaption(Profit) else deductionsCaption(Loss))),
      "govuk-!-margin-bottom-9")
  }

  def buildCapitalAllowancesTable(profitOrLoss: ProfitOrLoss,
                                  capitalAllowances: BigDecimal,
                                  turnoverNotTaxable: BigDecimal,
                                  totalDeductionsOrAdditions: BigDecimal)(implicit messages: Messages): Table = {

    val deductionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.capitalAllowances", capitalAllowances),
      buildTableAmountRow("profitOrLoss.turnoverNotTaxable", turnoverNotTaxable),
      buildTableAmountRow(
        if (profitOrLoss == Profit) { deductionsCaption(profitOrLoss) }
        else { additionsCaption(profitOrLoss) },
        totalDeductionsOrAdditions)
    )

    buildTable(None, deductionsRows, caption = Some(messages(if (profitOrLoss == Profit) deductionsCaption(Profit) else additionsCaption(Loss))))
  }

}
