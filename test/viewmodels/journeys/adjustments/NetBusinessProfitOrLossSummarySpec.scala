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
import models.common.UserType.Individual
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.adjustments.profitOrLoss.GoodsAndServicesAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary.doubleMarginClasses
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

  "buildTables must return all three NetBusinessProfitOrLossSummary tables" - {
    "when a net profit with Goods and Services session data" in {
      val goodsAndServicesForOwnUse = BigDecimal(111.22)
      val userAnswers               = buildUserAnswers(GoodsAndServicesAmountPage, goodsAndServicesForOwnUse)
      val result =
        NetBusinessProfitOrLossSummary.buildTables(netProfitValues, userAnswers, Profit, Individual, businessId)(messages)

      val netProfitLossTable = expectedNetProfitLossTable(
        Profit,
        netProfitValues.turnover,
        netProfitValues.incomeNotCountedAsTurnover,
        netProfitValues.totalExpenses,
        netProfitValues.netProfitOrLossAmount)
      val expensesTable = expectedExpensesTable(
        Profit,
        netProfitValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netProfitValues.disallowableExpenses,
        netProfitValues.totalAdditions)
      val capitalAllowancesTable = expectedCapitalAllowancesTable(
        Profit,
        netProfitValues.capitalAllowances,
        netProfitValues.turnoverNotTaxableAsBusinessProfit,
        netProfitValues.totalDeductions)

      assertWithClue(result.netProfitLossTable, expectedResult = netProfitLossTable)
      assertWithClue(result.expensesTable, expectedResult = expensesTable)
      assertWithClue(result.capitalAllowancesTable, expectedResult = capitalAllowancesTable)
    }
    "when a net loss and no Goods and Services session data" in {
      val goodsAndServicesForOwnUse = BigDecimal(0)
      val result =
        NetBusinessProfitOrLossSummary.buildTables(netLossValues, emptyUserAnswers, Loss, Individual, businessId)(messages)

      val netProfitLossTable = expectedNetProfitLossTable(
        Loss,
        netLossValues.turnover,
        netLossValues.incomeNotCountedAsTurnover,
        netLossValues.totalExpenses,
        netLossValues.netProfitOrLossAmount)
      val expensesTable = expectedExpensesTable(
        Loss,
        netLossValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netLossValues.disallowableExpenses,
        netLossValues.totalAdditions)
      val capitalAllowancesTable = expectedCapitalAllowancesTable(
        Loss,
        netLossValues.capitalAllowances,
        netLossValues.turnoverNotTaxableAsBusinessProfit,
        netLossValues.totalDeductions)

      assertWithClue(result.netProfitLossTable, expectedResult = netProfitLossTable)
      assertWithClue(result.expensesTable, expectedResult = expensesTable)
      assertWithClue(result.capitalAllowancesTable, expectedResult = capitalAllowancesTable)
    }
  }

  "buildNetProfitLossTable must create a Table with the correct content" - {
    "when a net profit" in {
      val table =
        NetBusinessProfitOrLossSummary.buildNetProfitOrLossTable(netProfitValues, Profit, doubleMarginClasses)(messages)
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
        NetBusinessProfitOrLossSummary.buildNetProfitOrLossTable(netLossValues, Loss, doubleMarginClasses)(messages)
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
    val goodsAndServicesForOwnUse = 111.22
    "when a net profit" in {
      val table =
        NetBusinessProfitOrLossSummary.buildExpensesTable(netProfitValues, goodsAndServicesForOwnUse, Profit, Individual, doubleMarginClasses)
      val expectedTable = expectedExpensesTable(
        Profit,
        netProfitValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netProfitValues.disallowableExpenses,
        netProfitValues.totalAdditions)
      val expectedCaption = Some(additionsCaption(Profit))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when a net loss" in {
      val table =
        NetBusinessProfitOrLossSummary.buildExpensesTable(netLossValues, goodsAndServicesForOwnUse, Loss, Individual, doubleMarginClasses)
      val expectedTable = expectedExpensesTable(
        Loss,
        netLossValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netLossValues.disallowableExpenses,
        netLossValues.totalAdditions)
      val expectedCaption = Some(deductionsCaption(Loss))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildCapitalAllowancesTable must create a Table with the correct content" - {
    "when a net profit" in {
      val table = NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netProfitValues, Profit, doubleMarginClasses)(messages)
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
      val table = NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netLossValues, Loss, doubleMarginClasses)(messages)
      val expectedTable = expectedCapitalAllowancesTable(
        Loss,
        netLossValues.capitalAllowances,
        netLossValues.turnoverNotTaxableAsBusinessProfit,
        netLossValues.totalDeductions
      )
      val expectedCaption = Some(additionsCaption(Loss))

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

}
