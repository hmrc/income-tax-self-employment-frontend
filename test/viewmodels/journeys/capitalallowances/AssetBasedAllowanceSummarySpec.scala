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
import builders.NetBusinessProfitOrLossValuesBuilder.{aNetBusinessLossValues, aNetBusinessProfitValues}
import models.journeys.adjustments.ProfitOrLoss
import org.scalatest
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import utils.MoneyUtils.formatPosNegMoneyWithPounds

class AssetBasedAllowanceSummarySpec extends SpecBase with TableDrivenPropertyChecks {

  private implicit val messages: Messages = messagesStubbed

  private def assertWithClue(result: Table, expectedResult: String): scalatest.Assertion = withClue(s"""
       |Result:
       |${result.rows.mkString("\n")}
       |did not equal expected result:
       |$expectedResult
       |""".stripMargin) {

    assert(result.rows.mkString("\n") === expectedResult)
  }

  "buildNetProfitOrLossTable" - {
    "must create a Table with the correct profit or loss content" - {
      "when net profit" in {
        val netBusinessProfitOrLossValues = aNetBusinessProfitValues
        val turnover                      = netBusinessProfitOrLossValues.turnover
        val incomeNotCountedAsTurnover    = netBusinessProfitOrLossValues.incomeNotCountedAsTurnover
        val totalExpenses                 = s"(£${netBusinessProfitOrLossValues.totalExpenses})"
        val netProfit                     = netBusinessProfitOrLossValues.netProfitOrLossAmount

        val table         = AssetBasedAllowanceSummary.buildNetProfitOrLossTable(aNetBusinessProfitValues)
        val expectedTable = expectedProfitTable(turnover, incomeNotCountedAsTurnover, totalExpenses, netProfit, ProfitOrLoss.Profit)

        assertWithClue(result = table, expectedResult = expectedTable)
      }

      "when net loss" in {
        val netBusinessProfitOrLossValues = aNetBusinessProfitValues
        val turnover                      = netBusinessProfitOrLossValues.turnover
        val incomeNotCountedAsTurnover    = netBusinessProfitOrLossValues.incomeNotCountedAsTurnover
        val totalExpenses                 = s"(£${netBusinessProfitOrLossValues.totalExpenses})"
        val netLoss                       = netBusinessProfitOrLossValues.netProfitOrLossAmount

        val table         = AssetBasedAllowanceSummary.buildNetProfitOrLossTable(aNetBusinessLossValues)
        val expectedTable = expectedProfitTable(turnover, incomeNotCountedAsTurnover, totalExpenses, netLoss, ProfitOrLoss.Loss)

        assertWithClue(result = table, expectedResult = expectedTable)
      }
    }
  }

  private def expectedProfitTable(turnover: BigDecimal,
                                  incomeNotCountedAsTurnover: BigDecimal,
                                  totalExpenses: String,
                                  netProfit: BigDecimal,
                                  profitOrLoss: ProfitOrLoss): String =
    s"""|List(TableRow(HtmlContent(profitOrLoss.turnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         turnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.incomeNotCountedAsTurnover),None,,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         incomeNotCountedAsTurnover)}),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.totalExpenses),None,,None,None,Map()), TableRow(HtmlContent($totalExpenses),None,govuk-!-text-align-right ,None,None,Map()))
        |List(TableRow(HtmlContent(profitOrLoss.netProfitOrLoss.$profitOrLoss),None,govuk-!-font-weight-bold,None,None,Map()), TableRow(HtmlContent(${formatPosNegMoneyWithPounds(
         netProfit)}),None,govuk-!-text-align-right govuk-!-font-weight-bold,None,None,Map()))""".stripMargin

}
