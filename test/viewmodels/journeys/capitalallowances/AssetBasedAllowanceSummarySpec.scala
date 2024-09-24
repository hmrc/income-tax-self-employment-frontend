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
import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
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
      "when net profit" in new Test {
        override def netBusinessProfitOrLossValues = aNetBusinessProfitValues
        override def profitOrLoss                  = ProfitOrLoss.Profit
      }

      "when net loss" in new Test {
        override def netBusinessProfitOrLossValues = aNetBusinessLossValues
        override def profitOrLoss                  = ProfitOrLoss.Loss
      }
    }
  }

  trait Test {
    def netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues
    def profitOrLoss: ProfitOrLoss

    def table         = AssetBasedAllowanceSummary.buildNetProfitOrLossTable(netBusinessProfitOrLossValues)
    def expectedTable = expectedProfitOrLossTable(turnover, incomeNotCountedAsTurnover, totalExpenses, netProfitOrLoss, profitOrLoss)

    val turnover                   = netBusinessProfitOrLossValues.turnover
    val incomeNotCountedAsTurnover = netBusinessProfitOrLossValues.incomeNotCountedAsTurnover
    val totalExpenses              = s"(£${netBusinessProfitOrLossValues.totalExpenses})"
    val netProfitOrLoss            = netBusinessProfitOrLossValues.netProfitOrLossAmount

    assertWithClue(result = table, expectedResult = expectedTable)
  }

  private def expectedProfitOrLossTable(turnover: BigDecimal,
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
