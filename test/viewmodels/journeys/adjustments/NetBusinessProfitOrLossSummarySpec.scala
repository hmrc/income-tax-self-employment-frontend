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
import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.Profit
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.formatPosNegMoneyWithPounds
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{additionsCaption, deductionsCaption}

class NetBusinessProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val netProfitLossTableScenariosProfit = Table(
    ("profitOrLoss", "turnover", "incomeNotCountedAsTurnover", "totalExpenses", "netProfitOrLoss"),
    (Profit, 200, 0.5, -50, 200)
  )
  private val expensesTableScenariosProfit = Table(
    ("profitOrLoss", "balancingCharge", "goodsAndServices", "disallowableExpenses", "additions"),
    (Profit, 200, 0.5, -50, 200)
  )
  private val capitalAllowancesTableScenarios = Table(
    ("profitOrLoss", "capitalAllowances", "turnoverNotTaxable", "deductions"),
    (Profit, 200, 0.5, 200)
  )

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {
    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildNetProfitLossTable must create a Table with the correct content" - {
    forAll(netProfitLossTableScenariosProfit) { case (profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses, netProfit) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildNetProfitLossTable(profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses, netProfit)(
            messages)
        val expectedTable   = expectedNetProfitLossTable(profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses, netProfit)
        val expectedCaption = Some(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")

        assert(table.caption == expectedCaption)
        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  "buildExpensesTable must create a Table with the correct content" - {
    forAll(expensesTableScenariosProfit) { case (profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses, additions) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildExpensesTable(profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses, additions)(
            messages)
        val expectedTable   = expectedExpensesTable(profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses, additions)
        val expectedCaption = Some(additionsCaption(Profit))

        assert(table.caption == expectedCaption)
        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  "buildCapitalAllowancesTable must create a Table with the correct content" - {
    forAll(capitalAllowancesTableScenarios) { case (profitOrLoss, capitalAllowances, turnoverNotTaxable, deductions) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(profitOrLoss, capitalAllowances, turnoverNotTaxable, deductions)(messages)
        val expectedTable   = expectedCapitalAllowancesTable(profitOrLoss, capitalAllowances, turnoverNotTaxable, deductions)
        val expectedCaption = Some(deductionsCaption(Profit))

        assert(table.caption == expectedCaption)
        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

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
    val additionsDeductionCaption = if (profitOrLoss == Profit) { additionsCaption(profitOrLoss) }
    else { deductionsCaption(profitOrLoss) }
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
    val deductionsAdditionsCaption = if (profitOrLoss == Profit) { deductionsCaption(profitOrLoss) }
    else { additionsCaption(profitOrLoss) }
    s"""|List(TableRow(HtmlContent(profitOrLoss.capitalAllowances),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         capitalAllowances)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOrLoss.turnoverNotTaxable),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnoverNotTaxable)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent($deductionsAdditionsCaption),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalDeductionsOrAdditions)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin
  }
}
