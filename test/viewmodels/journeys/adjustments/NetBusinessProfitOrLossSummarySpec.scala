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
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.{formatPosNegMoneyWithPounds, formatSumMoneyNoNegative}

class NetBusinessProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  // TODO SASS-8626 add cases for different amounts from backend, maybe change structure and test descriptions if needed when adding more scenarios
  private val table1Scenarios = Table(
    ("profitOrLoss", "turnover", "incomeNotCountedAsTurnover", "totalExpenses"),
    (ProfitOrLoss.Profit, 200, 0.5, -50),
    (ProfitOrLoss.Loss, -200, 10.01, 0)
  )
  private val table2Scenarios = Table(
    ("profitOrLoss", "balancingCharge", "goodsAndServices", "disallowableExpenses"),
    (ProfitOrLoss.Profit, 200, 0.5, -50),
    (ProfitOrLoss.Loss, -200, 10.01, 0)
  )
  private val table3Scenarios = Table(
    ("profitOrLoss", "capitalAllowances", "turnoverNotTaxable"),
    (ProfitOrLoss.Profit, 200, 0.5),
    (ProfitOrLoss.Loss, -200, 10.01)
  )

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {
    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildTable1 must create a Table with the correct content" - {
    forAll(table1Scenarios) { case (profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildTable1(profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses)(messages)
        val expectedTable = expectedTable1(profitOrLoss, turnover, incomeNotCountedAsTurnover, totalExpenses)

        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  "buildTable2 must create a Table with the correct content" - {
    forAll(table2Scenarios) { case (profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildTable2(profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses)(messages)
        val expectedTable = expectedTable2(profitOrLoss, balancingCharge, goodsAndServices, disallowableExpenses)

        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  "buildTable3 must create a Table with the correct content" - {
    forAll(table3Scenarios) { case (profitOrLoss, capitalAllowances, turnoverNotTaxable) =>
      s"when a net $profitOrLoss" in {
        val table =
          NetBusinessProfitOrLossSummary.buildTable3(profitOrLoss, capitalAllowances, turnoverNotTaxable)(messages)
        val expectedTable = expectedTable3(profitOrLoss, capitalAllowances, turnoverNotTaxable)

        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  def expectedTable1(profitOrLoss: ProfitOrLoss, turnover: BigDecimal, incomeNotCountedAsTurnover: BigDecimal, totalExpenses: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(profitOfLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(incomeNotCountedAsTurnover.title),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.netProfitOrLoss.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatSumMoneyNoNegative(
         List(turnover, incomeNotCountedAsTurnover, totalExpenses))}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

  def expectedTable2(profitOrLoss: ProfitOrLoss,
                     balancingCharge: BigDecimal,
                     goodsAndServices: BigDecimal,
                     disallowableExpenses: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(selectCapitalAllowances.balancingCharge),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         balancingCharge)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(goodsAndServicesForYourOwnUse.title.individual),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         goodsAndServices)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.disallowableExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         disallowableExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.totalAdditions.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatSumMoneyNoNegative(
         List(balancingCharge, goodsAndServices, disallowableExpenses))}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

  def expectedTable3(profitOrLoss: ProfitOrLoss, capitalAllowances: BigDecimal, turnoverNotTaxable: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(journeys.capital-allowances),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         capitalAllowances)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.turnoverNotTaxable),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnoverNotTaxable)}),None,govuk-!-text-align-right ,None,None,Map()))
      |List(TableRow(HtmlContent(profitOfLoss.totalDeductions.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatSumMoneyNoNegative(
         List(capitalAllowances, turnoverNotTaxable))}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

}
