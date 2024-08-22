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

import models.common.TaxYear
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

object AdjustedTaxableProfitSummary {

  // TODO all of the following hardcoded values will be replaced with API data from different places based on the calculations (SASS-9523)

  def buildTable1(taxYear: TaxYear)(implicit messages: Messages): Table = {

    val startYear = taxYear.startYear.toString
    val endYear   = taxYear.endYear.toString

    val adjustableTaxableProfitRows = Seq(
      buildTableAmountRow("profitOrLoss.netProfitOrLoss.profit", 4400.00),
      buildTableAmountRow("profitOrLoss.additions.profit", 200.00),
      buildTableAmountRow("profitOrLoss.deductions.profit", 0.00),
      buildTableAmountRow("profitOrLoss.netForTaxPurposes.profit", 4600.00),
      buildTableAmountRow("journeys.adjustments", 0.00),
      buildTableAmountRow(
        "profitOrLossCalculation.adjustedTable.profit",
        4600.00,
        classes = "govuk-!-font-weight-bold",
        optArgs = Seq(startYear, endYear))
    )
    buildTable(None, adjustableTaxableProfitRows)
  }

  def buildTable2()(implicit messages: Messages): Table = {

    val netProfit = Seq(
      buildTableAmountRow("profitOrLoss.turnover", 5000.00),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", 0.00),
      buildTableAmountRow("profitOrLoss.totalExpenses", -600.00),
      buildTableAmountRow("profitOrLoss.netProfitOrLoss.profit", 4400.25)
    )
    buildTable(None, netProfit, caption = Some(messages(s"profitOrLoss.netProfitOrLoss.profit")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildTable3()(implicit messages: Messages): Table = {

    val additionsToNetProfitTable = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", 0.00),
      buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", 200.00),
      buildTableAmountRow("profitOrLoss.disallowableExpenses", 0.00),
      buildTableAmountRow("profitOrLoss.totalAdditions.profit", 200.00)
    )
    buildTable(
      None,
      additionsToNetProfitTable,
      caption = Some(messages(s"profitOrLoss.additions.profit")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildTable4()(implicit messages: Messages): Table = {

    val capitalAllowanceTable = Seq(
      buildTableAmountRow("profitOrLoss.capitalAllowances", 0.00),
      buildTableAmountRow("profitOrLoss.turnoverNotTaxable", 0.00),
      buildTableAmountRow("profitOrLoss.totalDeductions.profit", 0.00)
    )
    buildTable(
      None,
      capitalAllowanceTable,
      caption = Some(messages(s"profitOrLoss.deductions.profit")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildTable5()(implicit messages: Messages): Table = {

    val adjustmentsTable = Seq(
      buildTableAmountRow("adjustments.anyOtherBusinessIncome", 0.00),
      buildTableAmountRow("adjustments.totalAdjustments", 0.00)
    )
    buildTable(None, adjustmentsTable, caption = Some(messages("journeys.adjustments")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }
}
