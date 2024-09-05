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

package viewmodels.journeys.capitalallowances

import base.SpecBase
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.formatPosNegMoneyWithPounds

class AssetBasedAllowanceSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  // TODO the hardcoded values are used to create the test cases, these can be updated when the values in Car or Asser based Allowance summary will be replaced with API data (SASS-8624)

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {

    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildCarsAndAssetBasedAllowanceTable must create a Table with the correct content" - {
    "when Profit" in {
      val turnover                   = 0.00
      val incomeNotCountedAsTurnover = 0.00
      val totalExpenses              = s"(£${0.00})"
      val netProfit                  = 12345.67

      val table         = AssetBasedAllowanceSummary.buildCarsAndAssetBasedAllowanceTable(Profit)
      val expectedTable = expectedBuildCarsAndAssetBasedAllowanceProfitTable(turnover, incomeNotCountedAsTurnover, totalExpenses, netProfit)

      assertWithClue(result = table, expectedResult = expectedTable)

    }

    "when Loss" in {
      val turnover                   = 0.00
      val incomeNotCountedAsTurnover = 0.00
      val totalExpenses              = s"(£${0.00})"
      val netLoss                    = 12345.67

      val table         = AssetBasedAllowanceSummary.buildCarsAndAssetBasedAllowanceTable(Loss)
      val expectedTable = expectedBuildCarsAndAssetBasedAllowanceLossTable(turnover, incomeNotCountedAsTurnover, totalExpenses, netLoss)

      assertWithClue(result = table, expectedResult = expectedTable)

    }

  }

  private def expectedBuildCarsAndAssetBasedAllowanceProfitTable(turnover: BigDecimal,
                                                                 incomeNotCountedAsTurnover: BigDecimal,
                                                                 totalExpenses: String,
                                                                 netProfit: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(profitOrLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.incomeNotCountedAsTurnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent(${totalExpenses}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.profit),None,govuk-!-font-weight-bold,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfit)}),None,govuk-!-text-align-right govuk-!-font-weight-bold,None,None,Map()))""".stripMargin

  private def expectedBuildCarsAndAssetBasedAllowanceLossTable(turnover: BigDecimal,
                                                               incomeNotCountedAsTurnover: BigDecimal,
                                                               totalExpenses: String,
                                                               netLoss: BigDecimal): String =
    s"""|List(TableRow(HtmlContent(profitOrLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.incomeNotCountedAsTurnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent(${totalExpenses}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.loss),None,govuk-!-font-weight-bold,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netLoss)}),None,govuk-!-text-align-right govuk-!-font-weight-bold,None,None,Map()))""".stripMargin

}
