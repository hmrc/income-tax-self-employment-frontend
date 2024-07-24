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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

object NetBusinessProfitOrLossSummary {

  private val placeholderAmount: BigDecimal = 200 // TODO this value is till SASS-8626 gets all values from backend

  def buildNetProfitTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val turnover                   = placeholderAmount
    val incomeNotCountedAsTurnover = placeholderAmount
    val totalExpenses              = placeholderAmount
    val netProfitOrLoss            = turnover + incomeNotCountedAsTurnover + totalExpenses
    // TODO separate calculations from table building when real values are added SASS-8626

    val netProfitRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOfLoss.turnover", turnover),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", incomeNotCountedAsTurnover),
      buildTableAmountRow("profitOfLoss.totalExpenses", totalExpenses),
      buildTableAmountRow(s"profitOfLoss.netProfitOrLoss.$profitOrLoss", netProfitOrLoss)
    )

    buildTable(
      None,
      netProfitRows,
      caption = Some(messages(s"profitOfLoss.netProfitOrLoss.$profitOrLoss")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildAdditionsTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val balancingCharge      = placeholderAmount
    val goodsAndServices     = placeholderAmount
    val disallowableExpenses = placeholderAmount
    val totalAdditions       = balancingCharge + goodsAndServices + disallowableExpenses

    val additionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", balancingCharge),
      buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", goodsAndServices),
      buildTableAmountRow("profitOfLoss.disallowableExpenses", disallowableExpenses),
      buildTableAmountRow(s"profitOfLoss.totalAdditions.$profitOrLoss", totalAdditions)
    )

    buildTable(None, additionsRows, caption = Some(messages(s"profitOfLoss.additions.$profitOrLoss")), "govuk-!-margin-bottom-9")
  }

  def buildDeductionsTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val capitalAllowances  = placeholderAmount
    val turnoverNotTaxable = placeholderAmount
    val totalDeductions    = capitalAllowances + turnoverNotTaxable

    val deductionsRows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("journeys.capital-allowances", capitalAllowances),
      buildTableAmountRow(s"profitOfLoss.turnoverNotTaxable.$profitOrLoss", turnoverNotTaxable),
      buildTableAmountRow(s"profitOfLoss.totalDeductions.$profitOrLoss", totalDeductions)
    )

    buildTable(None, deductionsRows, caption = Some(messages(s"profitOfLoss.deductions.$profitOrLoss")))
  }

}
