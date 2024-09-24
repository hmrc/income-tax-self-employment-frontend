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
import builders.NetBusinessProfitOrLossValuesBuilder.{aNetBusinessLossValues, aNetBusinessProfitValues}
import models.journeys.adjustments.ProfitOrLoss
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.formatPosNegMoneyWithPounds
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{additionsCaption, deductionsCaption}

class NetBusinessProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val netProfitValues = aNetBusinessProfitValues
  private val netLossValues   = aNetBusinessLossValues

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {
    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildNetProfitLossTable must create a Table with the correct content" - {
    "when a net profit" in {
      val table =
        NetBusinessProfitOrLossSummary.buildNetProfitLossTable(netProfitValues, Profit)(messages)
      val expectedTable = expectedNetProfitLossTable(
        Profit,
        netProfitValues.turnover,
        netProfitValues.incomeNotCountedAsTurnover,
        netProfitValues.totalExpenses,
        netProfitValues.netProfit)
      val expectedCaption = Some(s"profitOrLoss.netProfitOrLoss.profit")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when a net loss" in {
      val table =
        NetBusinessProfitOrLossSummary.buildNetProfitLossTable(netLossValues, Loss)(messages)
      val expectedTable = expectedNetProfitLossTable(
        Loss,
        netLossValues.turnover,
        netLossValues.incomeNotCountedAsTurnover,
        netLossValues.totalExpenses,
        netLossValues.netLoss)
      val expectedCaption = Some(s"profitOrLoss.netProfitOrLoss.loss")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildExpensesTable must create a Table with the correct content" - {
    "when a net profit" in {
      val table =
        NetBusinessProfitOrLossSummary.buildExpensesTable(netProfitValues, Profit)
      val expectedTable = expectedExpensesTable(
        Profit,
        netProfitValues.balancingCharge,
        netProfitValues.goodsAndServicesForOwnUse,
        netProfitValues.disallowableExpenses,
        netProfitValues.totalAdditions)
      val expectedCaption = Some(additionsCaption(Profit))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when a net loss" in {
      val table =
        NetBusinessProfitOrLossSummary.buildExpensesTable(netLossValues, Loss)
      val expectedTable = expectedExpensesTable(
        Loss,
        netLossValues.balancingCharge,
        netLossValues.goodsAndServicesForOwnUse,
        netLossValues.disallowableExpenses,
        netLossValues.totalAdditions)
      val expectedCaption = Some(deductionsCaption(Loss))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildCapitalAllowancesTable must create a Table with the correct content" - {
    "when a net profit" in {
      val table = NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netProfitValues, Profit)(messages)
      val expectedTable = expectedCapitalAllowancesTable(
        Profit,
        netProfitValues.capitalAllowances,
        netProfitValues.turnoverNotTaxableAsBusinessProfit,
        netProfitValues.totalDeductions
      )
      val expectedCaption = Some(deductionsCaption(Profit))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when a net Loss" in {
      val table = NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netLossValues, Loss)(messages)
      val expectedTable = expectedCapitalAllowancesTable(
        Loss,
        netProfitValues.capitalAllowances,
        netProfitValues.turnoverNotTaxableAsBusinessProfit,
        netProfitValues.totalDeductions
      )
      val expectedCaption = Some(additionsCaption(Loss))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
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
