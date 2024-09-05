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

package viewmodels.journeys.capitalallowances

import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow, buildTableRow}

object AssetBasedAllowanceSummary {

  // TODO the hardcoded values are used to create the test cases, these can be updated when the values in Car or Asser based Allowance summary will be replaced with API data (SASS-8624)

  def buildCarsAndAssetBasedAllowanceTable(profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table =
    profitOrLoss match {
      case Profit =>
        val tableRows = Seq(
          buildTableAmountRow("profitOrLoss.turnover", 0.00),
          buildTableAmountRow("profitOrLoss.incomeNotCountedAsTurnover", 0.00),
          buildTableRow("profitOrLoss.totalExpenses", s"(£${0.00})"),
          buildTableAmountRow("profitOrLoss.netProfitOrLoss.profit", 12345.67, classes = "govuk-!-font-weight-bold")
        )
        buildTable(None, tableRows)
      case Loss =>
        val tableRows = Seq(
          buildTableAmountRow("profitOrLoss.turnover", 0.00),
          buildTableAmountRow("profitOrLoss.incomeNotCountedAsTurnover", 0.00),
          buildTableRow("profitOrLoss.totalExpenses", s"(£${0.00})"),
          buildTableAmountRow("profitOrLoss.netProfitOrLoss.loss", 12345.67, classes = "govuk-!-font-weight-bold")
        )
        buildTable(None, tableRows)
    }
}
