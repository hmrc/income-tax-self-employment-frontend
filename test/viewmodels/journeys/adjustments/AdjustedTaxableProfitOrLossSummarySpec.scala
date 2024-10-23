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

//  "buildYourAdjustedProfitOrLossTable must create a Table with the correct content" - {
//    "when adjusted taxable profit" in {
//      val netProfit                           = 4400.00
//      val additionsToNetProfit                = 200.00
//      val deductionsFromNetProfit             = 0.00
//      val netBusinessProfit                   = 4600.00
//      val adjustments                         = 0.00
//      val adjustedTaxableProfitForCurrentYear = 4600.00
//
//      val table =
//        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(adjustedTaxableProfitForCurrentYear, adjustments, values, taxYear, Profit)
//      val expectedTable = expectedYourAdjustedProfitOrLossTable(
//        Profit,
//        netProfit,
//        additionsToNetProfit,
//        deductionsFromNetProfit,
//        netBusinessProfit,
//        adjustments,
//        adjustedTaxableProfitForCurrentYear)
//      assertWithClue(result = table, expectedResult = expectedTable)
//    }
//    "when adjusted loss" in {
//      val netLoss                    = 4400.00
//      val deductionsFromNetLoss      = 200.00
//      val additionsToNetLoss         = 0.00
//      val netBusinessLoss            = 4600.00
//      val adjustments                = 0.00
//      val adjustedLossForCurrentYear = 4600.00
//
//      val table =
//        AdjustedTaxableProfitOrLossSummary.buildYourAdjustedProfitOrLossTable(taxYear, Loss)
//      val expectedTable = expectedYourAdjustedProfitOrLossTable(
//        Loss,
//        netLoss,
//        deductionsFromNetLoss,
//        additionsToNetLoss,
//        netBusinessLoss,
//        adjustments,
//        adjustedLossForCurrentYear)
//      assertWithClue(result = table, expectedResult = expectedTable)
//    }
//  }
//
//  "buildAdjustmentsTable must create a Table with the correct content" - {
//    "when adjustments" in {
//      val anyOtherBusinessIncome = 0.00
//      val totalAdjustments       = 0.00
//
//      val table           = AdjustedTaxableProfitOrLossSummary.buildAdjustmentsTable()
//      val expectedTable   = expectedAdjustmentsTable(anyOtherBusinessIncome, totalAdjustments)
//      val expectedCaption = Some("journeys.adjustments")
//
//      assert(table.caption == expectedCaption)
//      assertWithClue(result = table, expectedResult = expectedTable)
//
//    }
//  }

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

  private def expectedAdjustmentsTable(anyOtherBusinessIncome: BigDecimal, totalAdjustments: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(adjustments.anyOtherBusinessIncome),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         anyOtherBusinessIncome)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(adjustments.totalAdjustments),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         totalAdjustments)}),None,govuk-!-text-align-right ,None,None,Map()))""".stripMargin

}
