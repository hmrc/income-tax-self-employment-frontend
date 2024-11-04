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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.marginBottomClass

class AdjustedTaxableProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val adjustments   = 500
  private val taxableProfit = 4400.00
  private val taxableLoss   = 2200.00

  private def assertWithClue(result: SummaryList, expectedResult: String): scalatest.Assertion = withClue(s"""
     |Result:
     |${result.rows.mkString("\n")}
     |did not equal expected result:
     |$expectedResult
     |""".stripMargin) {

    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildTables must return all five AdjustedTaxableProfitOrLossSummary tables" - {
    "when a net profit" in {
      val result =
        AdjustedTaxableProfitOrLossSummary.buildSummaryLists(taxableProfit, aNetBusinessProfitValues, taxYear, Profit, Individual)(messages)

      val yourAdjustedProfitOrLossSummaryList = expectedYourAdjustedProfitOrLossSummaryList(
        Profit,
        aNetBusinessProfitValues.netProfitOrLossAmount,
        aNetBusinessProfitValues.totalAdditions,
        aNetBusinessProfitValues.totalDeductions,
        aNetBusinessProfitValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableProfit
      )
      val netProfitOrLossSummaryList = expectedNetProfitLossSummaryList(
        Profit,
        aNetBusinessProfitValues.turnover,
        aNetBusinessProfitValues.incomeNotCountedAsTurnover,
        aNetBusinessProfitValues.totalExpenses,
        aNetBusinessProfitValues.netProfitOrLossAmount
      )
      val expensesSummaryList = expectedExpensesSummaryList(
        Profit,
        aNetBusinessProfitValues.balancingCharge,
        aNetBusinessProfitValues.goodsAndServicesForOwnUse,
        aNetBusinessProfitValues.disallowableExpenses,
        aNetBusinessProfitValues.totalAdditions
      )
      val capitalAllowancesSummaryList = expectedCapitalAllowancesSummaryList(
        Profit,
        aNetBusinessProfitValues.capitalAllowances,
        aNetBusinessProfitValues.turnoverNotTaxableAsBusinessProfit,
        aNetBusinessProfitValues.totalDeductions
      )
      val adjustmentsSummaryList = expectedAdjustmentsSummaryList(adjustments)

      assertWithClue(result.adjustedProfitOrLossSummaryList, expectedResult = yourAdjustedProfitOrLossSummaryList)
      assertWithClue(result.netProfitOrLossSummaryList, expectedResult = netProfitOrLossSummaryList)
      assertWithClue(result.expensesSummaryList, expectedResult = expensesSummaryList)
      assertWithClue(result.capitalAllowanceSummaryList, expectedResult = capitalAllowancesSummaryList)
      assertWithClue(result.adjustmentsSummaryList, expectedResult = adjustmentsSummaryList)
    }

    "when a net loss" in {
      val result =
        AdjustedTaxableProfitOrLossSummary.buildSummaryLists(taxableLoss, aNetBusinessLossValues, taxYear, Loss, Individual)(messages)

      val yourAdjustedProfitOrLossSummaryList = expectedYourAdjustedProfitOrLossSummaryList(
        Loss,
        aNetBusinessLossValues.netProfitOrLossAmount,
        aNetBusinessLossValues.totalAdditions,
        aNetBusinessLossValues.totalDeductions,
        aNetBusinessLossValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableLoss
      )
      val netProfitOrLossSummaryList = expectedNetProfitLossSummaryList(
        Loss,
        aNetBusinessLossValues.turnover,
        aNetBusinessLossValues.incomeNotCountedAsTurnover,
        aNetBusinessLossValues.totalExpenses,
        aNetBusinessLossValues.netProfitOrLossAmount
      )
      val expensesSummaryList = expectedExpensesSummaryList(
        Loss,
        aNetBusinessLossValues.balancingCharge,
        aNetBusinessLossValues.goodsAndServicesForOwnUse,
        aNetBusinessLossValues.disallowableExpenses,
        aNetBusinessLossValues.totalAdditions
      )
      val capitalAllowancesSummaryList = expectedCapitalAllowancesSummaryList(
        Loss,
        aNetBusinessLossValues.capitalAllowances,
        aNetBusinessLossValues.turnoverNotTaxableAsBusinessProfit,
        aNetBusinessLossValues.totalDeductions)
      val adjustmentsSummaryList = expectedAdjustmentsSummaryList(adjustments)

      assertWithClue(result.adjustedProfitOrLossSummaryList, expectedResult = yourAdjustedProfitOrLossSummaryList)
      assertWithClue(result.netProfitOrLossSummaryList, expectedResult = netProfitOrLossSummaryList)
      assertWithClue(result.expensesSummaryList, expectedResult = expensesSummaryList)
      assertWithClue(result.capitalAllowanceSummaryList, expectedResult = capitalAllowancesSummaryList)
      assertWithClue(result.adjustmentsSummaryList, expectedResult = adjustmentsSummaryList)
    }
  }

  "buildYourAdjustedProfitOrLossSummaryList must create a SummaryList with the correct content" - {
    "when a new profit" in {
      val summaryList =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossSummaryList(
          taxableProfit,
          adjustments,
          aNetBusinessProfitValues,
          taxYear,
          Profit)
      val expectedSummaryList = expectedYourAdjustedProfitOrLossSummaryList(
        Profit,
        aNetBusinessProfitValues.netProfitOrLossAmount,
        aNetBusinessProfitValues.totalAdditions,
        aNetBusinessProfitValues.totalDeductions,
        aNetBusinessProfitValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableProfit
      )

      assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
    }

    "when a net loss" in {
      val summary =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossSummaryList(taxableLoss, adjustments, aNetBusinessLossValues, taxYear, Loss)
      val expectedSummaryList = expectedYourAdjustedProfitOrLossSummaryList(
        Loss,
        aNetBusinessLossValues.netProfitOrLossAmount,
        aNetBusinessLossValues.totalAdditions,
        aNetBusinessLossValues.totalDeductions,
        aNetBusinessLossValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableLoss
      )

      assertWithClue(result = summary, expectedResult = expectedSummaryList)
    }
  }

  "buildAdjustmentsSummaryList must create a SummaryList with the correct content" in {
    val summaryList         = AdjustedTaxableProfitOrLossSummary.buildAdjustmentsSummaryList(adjustments, marginBottomClass)
    val expectedSummaryList = expectedAdjustmentsSummaryList(adjustments)

    assertWithClue(result = summaryList, expectedResult = expectedSummaryList)
  }

}
