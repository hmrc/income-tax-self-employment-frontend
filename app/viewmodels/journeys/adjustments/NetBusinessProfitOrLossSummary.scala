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

import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

case class NetBusinessProfitOrLossSummary(netProfitLossTable: Table, expensesTable: Table, capitalAllowancesTable: Table)

object NetBusinessProfitOrLossSummary {

  def additionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOrLoss.additions.$profitOrLoss"
  def deductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOrLoss.deductions.$profitOrLoss"

  def buildTables(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss)
                 (implicit messages: Messages): NetBusinessProfitOrLossSummary =
    NetBusinessProfitOrLossSummary(
      buildNetProfitLossTable(netBusinessProfitOrLossValues, profitOrLoss),
      buildExpensesTable(netBusinessProfitOrLossValues, profitOrLoss),
      buildCapitalAllowancesTable(netBusinessProfitOrLossValues, profitOrLoss)
    )

  def buildNetProfitLossTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.turnover", netBusinessProfitOrLossValues.turnover),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", netBusinessProfitOrLossValues.incomeNotCountedAsTurnover),
      buildTableAmountRow("profitOrLoss.totalExpenses", netBusinessProfitOrLossValues.totalExpenses),
      buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", netBusinessProfitOrLossValues.netProfitOrLossAmount)
    )

    buildTable(
      None,
      rows,
      caption = Some(messages(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildExpensesTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", netBusinessProfitOrLossValues.balancingCharge),
      buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", netBusinessProfitOrLossValues.goodsAndServicesForOwnUse),
      buildTableAmountRow("profitOrLoss.disallowableExpenses", netBusinessProfitOrLossValues.disallowableExpenses),
      buildTableAmountRow(
        if (profitOrLoss == Profit) { additionsCaption(profitOrLoss) }
        else { deductionsCaption(profitOrLoss) },
        netBusinessProfitOrLossValues.totalAdditions)
    )

    buildTable(
      None,
      rows,
      caption = Some(messages(if (profitOrLoss == Profit) additionsCaption(Profit) else deductionsCaption(Loss))),
      "govuk-!-margin-bottom-9")
  }

  def buildCapitalAllowancesTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.capitalAllowances", netBusinessProfitOrLossValues.capitalAllowances),
      buildTableAmountRow("profitOrLoss.turnoverNotTaxable", netBusinessProfitOrLossValues.turnoverNotTaxableAsBusinessProfit),
      buildTableAmountRow(
        if (profitOrLoss == Profit) { deductionsCaption(profitOrLoss) }
        else { additionsCaption(profitOrLoss) },
        netBusinessProfitOrLossValues.totalDeductions)
    )

    buildTable(None, rows, caption = Some(messages(if (profitOrLoss == Profit) deductionsCaption(Profit) else additionsCaption(Loss))))
  }

}
