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
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.formatPosNegMoneyWithPounds

class AdjustedTaxableProfitOrLossSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
     |Result:
     |${result.rows.mkString("\n")}
     |did not equal expected result:
     |$expectedResult
     |""".stripMargin) {

    assert(result.rows.mkString("\n") === expectedResult)
  }

  // TODO the hardcoded values are used to create the test cases, these can be updated when the values in AdjustedTaxableProfitSummary will be replaced with API data (SASS-9523)

  "buildYourAdjustedProfitOrLossTable must create a Table with the correct content" - {
    "when adjusted taxable profit" in {
      val netProfit                           = 4400.00
      val additionsToNetProfit                = 200.00
      val deductionsFromNetProfit             = 0.00
      val netBusinessProfit                   = 4600.00
      val adjustments                         = 0.00
      val adjustedTaxableProfitForCurrentYear = 4600.00

      val table =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(taxYear, Profit)
      val expectedTable = expectedYourAdjustedProfitOrLossTable(
        Profit,
        netProfit,
        additionsToNetProfit,
        deductionsFromNetProfit,
        netBusinessProfit,
        adjustments,
        adjustedTaxableProfitForCurrentYear)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when adjusted loss" in {
      val netLoss                    = 4400.00
      val deductionsFromNetLoss      = 200.00
      val additionsToNetLoss         = 0.00
      val netBusinessLoss            = 4600.00
      val adjustments                = 0.00
      val adjustedLossForCurrentYear = 4600.00

      val table =
        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(taxYear, Loss)
      val expectedTable = expectedYourAdjustedProfitOrLossTable(
        Loss,
        netLoss,
        deductionsFromNetLoss,
        additionsToNetLoss,
        netBusinessLoss,
        adjustments,
        adjustedLossForCurrentYear)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildNetProfitOrLossTable must create a Table with the correct content" - {
    "when net profit" in {
      val turnover                 = 5000.00
      val incomeNotCountedTurnover = 0.00
      val totalExpenses            = -600.00
      val netProfitOrLoss          = 4400.25

      val table           = AdjustedTaxableProfitOrLossSummary.buildNetProfitOrLossTable(Profit)
      val expectedTable   = expectedNetProfitOrLossTable(Profit, turnover, incomeNotCountedTurnover, totalExpenses, netProfitOrLoss)
      val expectedCaption = Some("profitOrLoss.netProfitOrLoss.profit")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when net loss" in {
      val turnover                 = 5000.00
      val incomeNotCountedTurnover = 0.00
      val totalExpenses            = -600.00
      val netProfitOrLoss          = 4400.25

      val table           = AdjustedTaxableProfitOrLossSummary.buildNetProfitOrLossTable(Loss)
      val expectedTable   = expectedNetProfitOrLossTable(Loss, turnover, incomeNotCountedTurnover, totalExpenses, netProfitOrLoss)
      val expectedCaption = Some("profitOrLoss.netProfitOrLoss.loss")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildExpensesTable must create a Table with the correct content" - {
    "when additions to net profit" in {
      val balancingCharge           = 0.00
      val goodsAndServicesForOwnUse = 0.00
      val disallowableExpenses      = 0.00
      val totalAdditions            = 0.00

      val table           = AdjustedTaxableProfitOrLossSummary.buildExpensesTable(Profit)
      val expectedTable   = expectedExpensesTable(Profit, balancingCharge, goodsAndServicesForOwnUse, disallowableExpenses, totalAdditions)
      val expectedCaption = Some("profitOrLoss.additions.profit")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when deductions from net loss" in {
      val balancingCharge           = 0.00
      val goodsAndServicesForOwnUse = 0.00
      val disallowableExpenses      = 0.00
      val totalAdditions            = 0.00

      val table           = AdjustedTaxableProfitOrLossSummary.buildExpensesTable(Loss)
      val expectedTable   = expectedExpensesTable(Loss, balancingCharge, goodsAndServicesForOwnUse, disallowableExpenses, totalAdditions)
      val expectedCaption = Some("profitOrLoss.deductions.loss")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildCapitalAllowanceTable must create a Table with the correct content" - {
    "when deductions from net profit" in {
      val capitalAllowance   = 0.00
      val turnoverNotTaxable = 0.00
      val totalDeductions    = 0.00

      val table           = AdjustedTaxableProfitOrLossSummary.buildCapitalAllowancesTable(Profit)
      val expectedTable   = expectedCapitalAllowanceTable(Profit, capitalAllowance, turnoverNotTaxable, totalDeductions)
      val expectedCaption = Some("profitOrLoss.deductions.profit")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
    "when additions to net loss" in {
      val capitalAllowance   = 0.00
      val turnoverNotTaxable = 0.00
      val totalDeductions    = 0.00

      val table           = AdjustedTaxableProfitOrLossSummary.buildCapitalAllowancesTable(Loss)
      val expectedTable   = expectedCapitalAllowanceTable(Loss, capitalAllowance, turnoverNotTaxable, totalDeductions)
      val expectedCaption = Some("profitOrLoss.additions.loss")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)
    }
  }

  "buildAdjustmentsTable must create a Table with the correct content" - {
    "when adjustments" in {
      val anyOtherBusinessIncome = 0.00
      val totalAdjustments       = 0.00

      val table           = AdjustedTaxableProfitOrLossSummary.buildAdjustmentsTable()
      val expectedTable   = expectedAdjustmentsTable(anyOtherBusinessIncome, totalAdjustments)
      val expectedCaption = Some("journeys.adjustments")

      assert(table.caption == expectedCaption)
      assertWithClue(result = table, expectedResult = expectedTable)

    }
  }

  private def expectedYourAdjustedProfitOrLossTable(profitOrLoss: ProfitOrLoss,
                                                    netProfit: BigDecimal,
                                                    additionsToNetProfit: BigDecimal,
                                                    deductionsFromNetProfit: BigDecimal,
                                                    netBusinessProfit: BigDecimal,
                                                    adjustments: BigDecimal,
                                                    adjustedTaxableProfitForCurrentYear: BigDecimal): String = {
    val additionsToProfitOrDeductionsFromLoss = if (profitOrLoss == Profit) { "additions.profit" }
    else { "deductions.loss" }
    val deductionsFromProfitOrAdditionsToLoss = if (profitOrLoss == Profit) { "deductions.profit" }
    else { "additions.loss" }
    s"""|List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfit)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.$additionsToProfitOrDeductionsFromLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         additionsToNetProfit)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.$deductionsFromProfitOrAdditionsToLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         deductionsFromNetProfit)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLossCalculation.adjustedTable.netForTaxPurposes.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netBusinessProfit)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(journeys.adjustments),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustments)}),None,govuk-!-text-align-right ,None,None,Map()))
    |List(TableRow(HtmlContent(profitOrLossCalculation.adjustedTable.adjustedTaxableProfitOrLoss.$profitOrLoss),None,govuk-!-font-weight-bold,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         adjustedTaxableProfitForCurrentYear)}),None,govuk-!-text-align-right govuk-!-font-weight-bold,None,None,Map()))""".stripMargin
  }

  private def expectedNetProfitOrLossTable(profitOrLoss: ProfitOrLoss,
                                           turnover: BigDecimal,
                                           incomeNotCountedTurnover: BigDecimal,
                                           totalExpenses: BigDecimal,
                                           netProfitOrLoss: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(profitOrLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(incomeNotCountedAsTurnover.title),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfitOrLoss)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

  private def expectedExpensesTable(profitOrLoss: ProfitOrLoss,
                                    balancingCharge: BigDecimal,
                                    goodsAndServicesForOwnUse: BigDecimal,
                                    disallowableExpenses: BigDecimal,
                                    totalAdditions: BigDecimal): String = {
    val profitTotalAdditionsOrLossTotalDeductions = if (profitOrLoss == Profit) { "totalAdditions.profit" }
    else { "totalDeductions.loss" }
    s"""|List(TableRow(HtmlContent(selectCapitalAllowances.balancingCharge),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         balancingCharge)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(goodsAndServicesForYourOwnUse.title.individual),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         goodsAndServicesForOwnUse)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.disallowableExpenses),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         disallowableExpenses)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.$profitTotalAdditionsOrLossTotalDeductions),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalAdditions)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin
  }

  private def expectedCapitalAllowanceTable(profitOrLoss: ProfitOrLoss,
                                            capitalAllowance: BigDecimal,
                                            turnoverNotTaxable: BigDecimal,
                                            totalDeductions: BigDecimal): String = {
    val profitTotalDeductionsOrLossTotalAdditions = if (profitOrLoss == Profit) { "totalDeductions.profit" }
    else { "totalAdditions.loss" }
    s"""|List(TableRow(HtmlContent(profitOrLoss.capitalAllowances),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         capitalAllowance)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.turnoverNotTaxable),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnoverNotTaxable)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.$profitTotalDeductionsOrLossTotalAdditions),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalDeductions)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin
  }

  private def expectedAdjustmentsTable(anyOtherBusinessIncome: BigDecimal, totalAdjustments: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(adjustments.anyOtherBusinessIncome),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         anyOtherBusinessIncome)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(adjustments.totalAdjustments),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalAdjustments)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

}
