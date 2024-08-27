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
import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

case class AdjustedTaxableProfitOrLossSummary(adjustedProfitOrLossTable: Table,
                                              netProfitOrLossTable: Table,
                                              expensesTable: Table,
                                              capitalAllowanceTable: Table,
                                              adjustmentsTable: Table)
object AdjustedTaxableProfitOrLossSummary {

  def buildTables(taxYear: TaxYear, profitOrLoss: ProfitOrLoss)(implicit messages: Messages): AdjustedTaxableProfitOrLossSummary =
    AdjustedTaxableProfitOrLossSummary(
      buildYourAdjustedProfitOrLossTable(taxYear, profitOrLoss),
      buildNetProfitOrLossTable(profitOrLoss),
      buildExpensesTable(profitOrLoss),
      buildCapitalAllowancesTable(profitOrLoss),
      buildAdjustmentsTable()
    )
  def buildYourAdjustedProfitOrLossTable(taxYear: TaxYear, profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {

    val startYear = taxYear.startYear.toString
    val endYear   = taxYear.endYear.toString

    // TODO all of the following hardcoded values will be replaced with API data from different places based on the calculations (SASS-9523)

    profitOrLoss match {
      case Profit =>
        val rows = Seq(
          buildTableAmountRow("profitOrLoss.netProfitOrLoss.profit", 4400.00),
          buildTableAmountRow("profitOrLoss.additions.profit", 200.00),
          buildTableAmountRow("profitOrLoss.deductions.profit", 0.00),
          buildTableAmountRow("profitOrLossCalculation.adjustedTable.netForTaxPurposes.profit", 4600.00),
          buildTableAmountRow("journeys.adjustments", 0.00),
          buildTableAmountRow(
            "profitOrLossCalculation.adjustedTable.adjustedTaxableProfitOrLoss.profit",
            4600.00,
            classes = "govuk-!-font-weight-bold",
            optArgs = Seq(startYear, endYear))
        )
        buildTable(None, rows)
      case Loss =>
        val rows = Seq(
          buildTableAmountRow("profitOrLoss.netProfitOrLoss.loss", 4400.00),
          buildTableAmountRow("profitOrLoss.deductions.loss", 200.00),
          buildTableAmountRow("profitOrLoss.additions.loss", 0.00),
          buildTableAmountRow("profitOrLossCalculation.adjustedTable.netForTaxPurposes.loss", 4600.00),
          buildTableAmountRow("journeys.adjustments", 0.00),
          buildTableAmountRow(
            "profitOrLossCalculation.adjustedTable.adjustedTaxableProfitOrLoss.loss",
            4600,
            "govuk-!-font-weight-bold",
            optArgs = Seq(startYear, endYear))
        )
        buildTable(None, rows)
    }
  }

  def buildNetProfitOrLossTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {
    val rows = Seq(
      buildTableAmountRow("profitOrLoss.turnover", 5000.00),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", 0.00),
      buildTableAmountRow("profitOrLoss.totalExpenses", -600.00),
      buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", 4400.25)
    )
    buildTable(None, rows, caption = Some(messages(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }

  def buildExpensesTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table =
    profitOrLoss match {
      case Profit =>
        val rows = Seq(
          buildTableAmountRow("selectCapitalAllowances.balancingCharge", 0.00),
          buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", 0.00),
          buildTableAmountRow("profitOrLoss.disallowableExpenses", 0.00),
          buildTableAmountRow("profitOrLoss.totalAdditions.profit", 0.00)
        )
        buildTable(None, rows, caption = Some(messages("profitOrLoss.additions.profit")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
      case Loss =>
        val rows = Seq(
          buildTableAmountRow("selectCapitalAllowances.balancingCharge", 0),
          buildTableAmountRow("goodsAndServicesForYourOwnUse.title.individual", 0),
          buildTableAmountRow("profitOrLoss.disallowableExpenses", 0),
          buildTableAmountRow("profitOrLoss.totalDeductions.loss", 0)
        )

        buildTable(None, rows, caption = Some(messages("profitOrLoss.deductions.loss")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")

    }

  def buildCapitalAllowancesTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table =
    profitOrLoss match {
      case Profit =>
        val rows = Seq(
          buildTableAmountRow("profitOrLoss.capitalAllowances", 0.00),
          buildTableAmountRow("profitOrLoss.turnoverNotTaxable", 0.00),
          buildTableAmountRow("profitOrLoss.totalDeductions.profit", 0.00)
        )
        buildTable(None, rows, caption = Some(messages("profitOrLoss.deductions.profit")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
      case Loss =>
        val rows = Seq(
          buildTableAmountRow("profitOrLoss.capitalAllowances", 0),
          buildTableAmountRow("profitOrLoss.turnoverNotTaxable", 0),
          buildTableAmountRow("profitOrLoss.totalAdditions.loss", 0)
        )

        buildTable(None, rows, caption = Some(messages("profitOrLoss.additions.loss")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
    }

  def buildAdjustmentsTable()(implicit messages: Messages): Table = {

    val rows = Seq(
      buildTableAmountRow("adjustments.anyOtherBusinessIncome", 0.00),
      buildTableAmountRow("adjustments.totalAdjustments", 0.00)
    )
    buildTable(None, rows, caption = Some(messages("journeys.adjustments")), "govuk-!-margin-top-6 govuk-!-margin-bottom-9")
  }
}
