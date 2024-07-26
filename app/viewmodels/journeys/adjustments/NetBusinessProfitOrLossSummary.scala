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

import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow, buildTableRow}

object NetBusinessProfitOrLossSummary {

  def additionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOfLoss.additions.$profitOrLoss"
  def deductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOfLoss.deductions.$profitOrLoss"

  def buildTable1(profitOrLoss: ProfitOrLoss, turnover: BigDecimal, incomeNotCountedAsTurnover: BigDecimal, totalExpenses: BigDecimal)(implicit
      messages: Messages): Table = {

    val netProfitOrLoss = formatSumMoneyNoNegative(List(turnover, incomeNotCountedAsTurnover, totalExpenses))

    val netProfitOrLossRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOfLoss.turnover", turnover),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", incomeNotCountedAsTurnover),
      buildTableAmountRow("profitOfLoss.totalExpenses", totalExpenses),
      buildTableRow(s"profitOfLoss.netProfitOrLoss.$profitOrLoss", netProfitOrLoss)
    )

    buildTable(
      None,
      netProfitOrLossRows,
      caption = Some(messages(s"profitOfLoss.netProfitOrLoss.$profitOrLoss")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildTable2(profitOrLoss: ProfitOrLoss, balancingCharge: BigDecimal, goodsAndServices: BigDecimal, disallowableExpenses: BigDecimal)(implicit
      messages: Messages): Table = {

    val totalAdditions = formatSumMoneyNoNegative(List(balancingCharge, goodsAndServices, disallowableExpenses))

    val additionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", balancingCharge),
      buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", goodsAndServices),
      buildTableAmountRow("profitOfLoss.disallowableExpenses", disallowableExpenses),
      buildTableRow(s"profitOfLoss.totalAdditions.$profitOrLoss", totalAdditions)
    )

    buildTable(
      None,
      additionsRows,
      caption = Some(messages(if (profitOrLoss == Profit) additionsCaption(Profit) else deductionsCaption(Loss))),
      "govuk-!-margin-bottom-9")
  }

  def buildTable3(profitOrLoss: ProfitOrLoss, capitalAllowances: BigDecimal, turnoverNotTaxable: BigDecimal)(implicit messages: Messages): Table = {

    val totalDeductions = formatSumMoneyNoNegative(List(capitalAllowances, turnoverNotTaxable))

    val deductionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("journeys.capital-allowances", capitalAllowances),
      buildTableAmountRow("profitOfLoss.turnoverNotTaxable", turnoverNotTaxable),
      buildTableRow(s"profitOfLoss.totalDeductions.$profitOrLoss", totalDeductions)
    )

    buildTable(None, deductionsRows, caption = Some(messages(if (profitOrLoss == Profit) deductionsCaption(Profit) else additionsCaption(Loss))))
  }

}
