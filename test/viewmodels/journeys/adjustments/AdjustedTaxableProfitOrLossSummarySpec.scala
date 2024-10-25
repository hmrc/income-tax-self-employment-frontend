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
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import viewmodels.journeys.adjustments.AdjustedTaxableProfitOrLossSummary.doubleMarginClasses

class AdjustedTaxableProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private val adjustments   = 500
  private val taxableProfit = 4400.00
  private val taxableLoss   = 2200.00

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
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
        AdjustedTaxableProfitOrLossSummary.buildTables(taxableProfit, aNetBusinessProfitValues, taxYear, Profit, Individual)(messages)

      val yourAdjustedProfitOrLossTable = expectedYourAdjustedProfitOrLossTable(
        Profit,
        aNetBusinessProfitValues.netProfitOrLossAmount,
        aNetBusinessProfitValues.totalAdditions,
        aNetBusinessProfitValues.totalDeductions,
        aNetBusinessProfitValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableProfit
      )
      val netProfitOrLossTable = expectedNetProfitLossTable(
        Profit,
        aNetBusinessProfitValues.turnover,
        aNetBusinessProfitValues.incomeNotCountedAsTurnover,
        aNetBusinessProfitValues.totalExpenses,
        aNetBusinessProfitValues.netProfitOrLossAmount
      )
      val expensesTable = expectedExpensesTable(
        Profit,
        aNetBusinessProfitValues.balancingCharge,
        aNetBusinessProfitValues.goodsAndServicesForOwnUse,
        aNetBusinessProfitValues.disallowableExpenses,
        aNetBusinessProfitValues.totalAdditions
      )
      val capitalAllowancesTable = expectedCapitalAllowancesTable(
        Profit,
        aNetBusinessProfitValues.capitalAllowances,
        aNetBusinessProfitValues.turnoverNotTaxableAsBusinessProfit,
        aNetBusinessProfitValues.totalDeductions
      )
      val adjustmentsTable = expectedAdjustmentsTable(adjustments)

      assertWithClue(result.adjustedProfitOrLossTable, expectedResult = yourAdjustedProfitOrLossTable)
      assertWithClue(result.netProfitOrLossTable, expectedResult = netProfitOrLossTable)
      assertWithClue(result.expensesTable, expectedResult = expensesTable)
      assertWithClue(result.capitalAllowanceTable, expectedResult = capitalAllowancesTable)
      assertWithClue(result.adjustmentsTable, expectedResult = adjustmentsTable)
    }

    "when a net loss" in {
      val result =
        AdjustedTaxableProfitOrLossSummary.buildTables(taxableLoss, aNetBusinessLossValues, taxYear, Loss, Individual)(messages)

      val yourAdjustedProfitOrLossTable = expectedYourAdjustedProfitOrLossTable(
        Loss,
        aNetBusinessLossValues.netProfitOrLossAmount,
        aNetBusinessLossValues.totalAdditions,
        aNetBusinessLossValues.totalDeductions,
        aNetBusinessLossValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableLoss
      )
      val netProfitOrLossTable = expectedNetProfitLossTable(
        Loss,
        aNetBusinessLossValues.turnover,
        aNetBusinessLossValues.incomeNotCountedAsTurnover,
        aNetBusinessLossValues.totalExpenses,
        aNetBusinessLossValues.netProfitOrLossAmount
      )
      val expensesTable = expectedExpensesTable(
        Loss,
        aNetBusinessLossValues.balancingCharge,
        aNetBusinessLossValues.goodsAndServicesForOwnUse,
        aNetBusinessLossValues.disallowableExpenses,
        aNetBusinessLossValues.totalAdditions
      )
      val capitalAllowancesTable = expectedCapitalAllowancesTable(
        Loss,
        aNetBusinessLossValues.capitalAllowances,
        aNetBusinessLossValues.turnoverNotTaxableAsBusinessProfit,
        aNetBusinessLossValues.totalDeductions)
      val adjustmentsTable = expectedAdjustmentsTable(adjustments)

      assertWithClue(result.adjustedProfitOrLossTable, expectedResult = yourAdjustedProfitOrLossTable)
      assertWithClue(result.netProfitOrLossTable, expectedResult = netProfitOrLossTable)
      assertWithClue(result.expensesTable, expectedResult = expensesTable)
      assertWithClue(result.capitalAllowanceTable, expectedResult = capitalAllowancesTable)
      assertWithClue(result.adjustmentsTable, expectedResult = adjustmentsTable)
    }
  }

  "buildYourAdjustedProfitOrLossTable must create a Table with the correct content" - {
    "when a new profit" in {
      val table =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(taxableProfit, adjustments, aNetBusinessProfitValues, taxYear, Profit)
      val expectedTable = expectedYourAdjustedProfitOrLossTable(
        Profit,
        aNetBusinessProfitValues.netProfitOrLossAmount,
        aNetBusinessProfitValues.totalAdditions,
        aNetBusinessProfitValues.totalDeductions,
        aNetBusinessProfitValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableProfit
      )

      assertWithClue(result = table, expectedResult = expectedTable)
    }

    "when a net loss" in {
      val table =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(taxableLoss, adjustments, aNetBusinessLossValues, taxYear, Loss)
      val expectedTable = expectedYourAdjustedProfitOrLossTable(
        Loss,
        aNetBusinessLossValues.netProfitOrLossAmount,
        aNetBusinessLossValues.totalAdditions,
        aNetBusinessLossValues.totalDeductions,
        aNetBusinessLossValues.getNetBusinessProfitOrLossForTaxPurposes,
        adjustments,
        taxableLoss
      )

      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildAdjustmentsTable must create a Table with the correct content" in {
    val table           = AdjustedTaxableProfitOrLossSummary.buildAdjustmentsTable(adjustments, doubleMarginClasses)
    val expectedTable   = expectedAdjustmentsTable(adjustments)
    val expectedCaption = Some("journeys.adjustments")

    assert(table.caption == expectedCaption)
    assertWithClue(result = table, expectedResult = expectedTable)
  }

}
