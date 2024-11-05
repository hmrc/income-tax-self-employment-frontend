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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.marginBottomClass

class NetBusinessProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val netProfitValues = aNetBusinessProfitValues
  private val netLossValues   = aNetBusinessLossValues

  private def assertWithClue(result: SummaryList, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {
    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildSummaryLists must return all three NetBusinessProfitOrLossSummary SummaryLists" - {
    "when a net profit with Goods and Services session data" in {
      val goodsAndServicesForOwnUse = BigDecimal(111.22)
      val userAnswers               = buildUserAnswers(GoodsAndServicesAmountPage, goodsAndServicesForOwnUse)
      val result =
        NetBusinessProfitOrLossSummary.buildSummaryLists(netProfitValues, userAnswers, Profit, Individual, businessId)(messages)

      val netProfitLossSummaryList = expectedNetProfitLossSummaryList(
        Profit,
        netProfitValues.turnover,
        netProfitValues.incomeNotCountedAsTurnover,
        netProfitValues.totalExpenses,
        netProfitValues.netProfitOrLossAmount)
      val expensesSummaryList = expectedExpensesSummaryList(
        Profit,
        netProfitValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netProfitValues.disallowableExpenses,
        netProfitValues.totalAdditions)
      val capitalAllowancesSummaryList = expectedCapitalAllowancesSummaryList(
        Profit,
        netProfitValues.capitalAllowances,
        netProfitValues.turnoverNotTaxableAsBusinessProfit,
        netProfitValues.totalDeductions)

      assertWithClue(result.netProfitLossSummaryList, expectedResult = netProfitLossSummaryList)
      assertWithClue(result.expensesSummaryList, expectedResult = expensesSummaryList)
      assertWithClue(result.capitalAllowancesSummaryList, expectedResult = capitalAllowancesSummaryList)
    }
    "when a net loss and no Goods and Services session data" in {
      val goodsAndServicesForOwnUse = BigDecimal(0)
      val result =
        NetBusinessProfitOrLossSummary.buildSummaryLists(netLossValues, emptyUserAnswers, Loss, Individual, businessId)(messages)

      val netProfitLossSummaryList = expectedNetProfitLossSummaryList(
        Loss,
        netLossValues.turnover,
        netLossValues.incomeNotCountedAsTurnover,
        netLossValues.totalExpenses,
        netLossValues.netProfitOrLossAmount)
      val expensesSummaryList = expectedExpensesSummaryList(
        Loss,
        netLossValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netLossValues.disallowableExpenses,
        netLossValues.totalAdditions)
      val capitalAllowancesSummaryList = expectedCapitalAllowancesSummaryList(
        Loss,
        netLossValues.capitalAllowances,
        netLossValues.turnoverNotTaxableAsBusinessProfit,
        netLossValues.totalDeductions)

      assertWithClue(result.netProfitLossSummaryList, expectedResult = netProfitLossSummaryList)
      assertWithClue(result.expensesSummaryList, expectedResult = expensesSummaryList)
      assertWithClue(result.capitalAllowancesSummaryList, expectedResult = capitalAllowancesSummaryList)
    }
  }

  "buildNetProfitLossSummaryList must create a SummaryList with the correct content" - {
    "when a net profit" in {
      val summaryList =
        NetBusinessProfitOrLossSummary.buildNetProfitOrLossSummaryList(netProfitValues, Profit, marginBottomClass)(messages)
      val expectedSummaryList = expectedNetProfitLossSummaryList(
        Profit,
        netProfitValues.turnover,
        netProfitValues.incomeNotCountedAsTurnover,
        netProfitValues.totalExpenses,
        netProfitValues.netProfit)

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
    "when a net loss" in {
      val summaryList =
        NetBusinessProfitOrLossSummary.buildNetProfitOrLossSummaryList(netLossValues, Loss, marginBottomClass)(messages)
      val expectedSummaryList = expectedNetProfitLossSummaryList(
        Loss,
        netLossValues.turnover,
        netLossValues.incomeNotCountedAsTurnover,
        netLossValues.totalExpenses,
        netLossValues.netLoss)

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
  }

  "buildExpensesSummaryList must create a SummaryList with the correct content" - {
    val goodsAndServicesForOwnUse = 111.22
    "when a net profit" in {
      val summaryList =
        NetBusinessProfitOrLossSummary.buildExpensesSummaryList(netProfitValues, goodsAndServicesForOwnUse, Profit, Individual, marginBottomClass)
      val expectedSummaryList = expectedExpensesSummaryList(
        Profit,
        netProfitValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netProfitValues.disallowableExpenses,
        netProfitValues.totalAdditions)

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
    "when a net loss" in {
      val summaryList =
        NetBusinessProfitOrLossSummary.buildExpensesSummaryList(netLossValues, goodsAndServicesForOwnUse, Loss, Individual, marginBottomClass)
      val expectedSummaryList = expectedExpensesSummaryList(
        Loss,
        netLossValues.balancingCharge,
        goodsAndServicesForOwnUse,
        netLossValues.disallowableExpenses,
        netLossValues.totalAdditions)

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
  }

  "buildCapitalAllowancesSummaryList must create a SummaryList with the correct content" - {
    "when a net profit" in {
      val summaryList = NetBusinessProfitOrLossSummary.buildCapitalAllowancesSummaryList(netProfitValues, Profit, marginBottomClass)(messages)
      val expectedSummaryList = expectedCapitalAllowancesSummaryList(
        Profit,
        netProfitValues.capitalAllowances,
        netProfitValues.turnoverNotTaxableAsBusinessProfit,
        netProfitValues.totalDeductions
      )

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
    "when a net Loss" in {
      val summaryList = NetBusinessProfitOrLossSummary.buildCapitalAllowancesSummaryList(netLossValues, Loss, marginBottomClass)(messages)
      val expectedSummaryList = expectedCapitalAllowancesSummaryList(
        Loss,
        netLossValues.capitalAllowances,
        netLossValues.turnoverNotTaxableAsBusinessProfit,
        netLossValues.totalDeductions
      )

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }
  }

}
