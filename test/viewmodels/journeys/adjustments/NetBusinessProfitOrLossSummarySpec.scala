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

import base.SpecBase
import models.journeys.adjustments.ProfitOrLoss.Profit
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummarySpec.{expectedAdditionsTable, expectedDeductionsTable, expectedNetProfitTable}

class NetBusinessProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val testScenarios = Table(
    ("profitOrLoss", "expectedProfitTable", "expectedAdjustmentsTable", "expectedDeductionsTable"),
    (Profit, expectedNetProfitTable(), expectedAdditionsTable(), expectedDeductionsTable())
    // TODO SASS-9032 add cases for 'Loss' scenario
    // TODO SASS-8626 add cases for different amounts from backend (replace 'defaultAmount' in object builders below)
  )

  private def assertWithClue(result: Table, expectedResult: Table): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |${expectedResult.rows.mkString("\n")}
       |""".stripMargin) {
    assert(result === expectedResult)
  }

  forAll(testScenarios) { case (profitOrLoss, expectedProfitTable, expectedAdjustmentsTable, expectedDeductionsTable) =>
    // TODO SASS-8626 maybe change structure and test descriptions if needed when adding more scenarios
    "buildNetProfitTable must create a Table with the correct profit or loss specific content" in {
      val table = NetBusinessProfitOrLossSummary.buildNetProfitTable(profitOrLoss)(messages)

      assertWithClue(result = table, expectedResult = expectedProfitTable)
    }
    "buildAdditionsTable must create a Table with the correct profit or loss specific content" in {
      val table = NetBusinessProfitOrLossSummary.buildAdditionsTable(profitOrLoss)(messages)

      assertWithClue(result = table, expectedResult = expectedAdjustmentsTable)
    }
    "buildDeductionsTable must create a Table with the correct profit or loss specific content" in {
      val table = NetBusinessProfitOrLossSummary.buildDeductionsTable(profitOrLoss)(messages)

      assertWithClue(result = table, expectedResult = expectedDeductionsTable)
    }
  }
}

object NetBusinessProfitOrLossSummarySpec {

  private val defaultAmount: BigDecimal = 200

  def expectedNetProfitTable()(implicit messages: Messages): Table =
    buildTable(
      headRow = None,
      rows = Seq(
        buildTableAmountRow("profitOfLoss.turnover", defaultAmount),
        buildTableAmountRow("incomeNotCountedAsTurnover.title", defaultAmount),
        buildTableAmountRow("profitOfLoss.totalExpenses", defaultAmount),
        buildTableAmountRow("profitOfLoss.netProfitOrLoss.profit", BigDecimal(600))
      ),
      caption = Some(messages("profitOfLoss.netProfitOrLoss.profit")),
      "govuk-!-margin-top-6 govuk-!-margin-bottom-9"
    )

  def expectedAdditionsTable()(implicit messages: Messages): Table =
    buildTable(
      headRow = None,
      rows = Seq(
        buildTableAmountRow("selectCapitalAllowances.balancingCharge", defaultAmount),
        buildTableAmountRow("profitOfLoss.goodsAndServices", defaultAmount),
        buildTableAmountRow("profitOfLoss.disallowableExpenses", defaultAmount),
        buildTableAmountRow("profitOfLoss.totalAdditions.profit", BigDecimal(600))
      ),
      caption = Some(messages("profitOfLoss.additions.profit")),
      "govuk-!-margin-bottom-9"
    )

  def expectedDeductionsTable()(implicit messages: Messages): Table =
    buildTable(
      headRow = None,
      rows = Seq(
        buildTableAmountRow("journeys.capital-allowances", defaultAmount),
        buildTableAmountRow("profitOfLoss.turnoverNotTaxable.profit", defaultAmount),
        buildTableAmountRow("profitOfLoss.totalDeductions.profit", BigDecimal(400))
      ),
      caption = Some(messages("profitOfLoss.deductions.profit"))
    )

}
