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

import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.adjustments.ProfitOrLoss.Profit
import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
import pages.adjustments.profitOrLoss.GoodsAndServicesAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildBigDecimalKeyValueRow

case class NetBusinessProfitOrLossSummary(netProfitLossSummaryList: SummaryList,
                                          expensesSummaryList: SummaryList,
                                          capitalAllowancesSummaryList: SummaryList)

object NetBusinessProfitOrLossSummary {

  def totalAdditionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOrLoss.totalAdditions.$profitOrLoss"
  def totalDeductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOrLoss.totalDeductions.$profitOrLoss"
  def additionsCaption(profitOrLoss: ProfitOrLoss)       = s"profitOrLoss.additions.$profitOrLoss"
  def deductionsCaption(profitOrLoss: ProfitOrLoss)      = s"profitOrLoss.deductions.$profitOrLoss"

  def buildSummaryLists(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                        userAnswers: UserAnswers,
                        profitOrLoss: ProfitOrLoss,
                        userType: UserType,
                        businessId: BusinessId)(implicit messages: Messages): NetBusinessProfitOrLossSummary = {
    val goodsAndServicesForOwnUse: BigDecimal = userAnswers.get(GoodsAndServicesAmountPage, businessId).getOrElse(0)
    NetBusinessProfitOrLossSummary(
      buildNetProfitOrLossSummaryList(netBusinessProfitOrLossValues, profitOrLoss, "govuk-!-margin-top-6 govuk-!-margin-bottom-9"),
      buildExpensesSummaryList(netBusinessProfitOrLossValues, goodsAndServicesForOwnUse, profitOrLoss, userType, "govuk-!-margin-bottom-9"),
      buildCapitalAllowancesSummaryList(netBusinessProfitOrLossValues, profitOrLoss, "")
    )
  }

  def buildNetProfitOrLossSummaryList(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                                      profitOrLoss: ProfitOrLoss,
                                      SummaryListClasses: String)(implicit messages: Messages): SummaryList = {

    val rows: Seq[SummaryListRow] = Seq(
      buildBigDecimalKeyValueRow("profitOrLoss.turnover", netBusinessProfitOrLossValues.turnover),
      buildBigDecimalKeyValueRow("incomeNotCountedAsTurnover.title", netBusinessProfitOrLossValues.incomeNotCountedAsTurnover),
      buildBigDecimalKeyValueRow("profitOrLoss.totalExpenses", netBusinessProfitOrLossValues.totalExpenses),
      buildBigDecimalKeyValueRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", netBusinessProfitOrLossValues.netProfitOrLossAmount)
    )

    SummaryList(rows)
//    buildSummaryList(None, rows, caption = Some(messages(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")), SummaryListClasses)
  }

  def buildExpensesSummaryList(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                               goodsAndServicesForOwnUse: BigDecimal,
                               profitOrLoss: ProfitOrLoss,
                               userType: UserType,
                               SummaryListClasses: String)(implicit messages: Messages): SummaryList = {
    def additionsCaptionIfProfit: String = if (profitOrLoss == Profit) totalAdditionsCaption(profitOrLoss) else totalDeductionsCaption(profitOrLoss)
    def additionsTitleIfProfit: String   = if (profitOrLoss == Profit) additionsCaption(profitOrLoss) else deductionsCaption(profitOrLoss)
    val rows: Seq[SummaryListRow] = Seq(
      buildBigDecimalKeyValueRow("selectCapitalAllowances.balancingCharge", netBusinessProfitOrLossValues.balancingCharge),
      buildBigDecimalKeyValueRow(s"goodsAndServicesForYourOwnUse.title.$userType", goodsAndServicesForOwnUse),
      buildBigDecimalKeyValueRow("profitOrLoss.disallowableExpenses", netBusinessProfitOrLossValues.disallowableExpenses),
      buildBigDecimalKeyValueRow(additionsCaptionIfProfit, netBusinessProfitOrLossValues.totalAdditions)
    )

    SummaryList(rows)
//    buildSummaryList(None, rows, caption = Some(messages(additionsTitleIfProfit)), SummaryListClasses)
  }

  def buildCapitalAllowancesSummaryList(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                                        profitOrLoss: ProfitOrLoss,
                                        SummaryListClasses: String)(implicit messages: Messages): SummaryList = {
    def deductionsCaptionIfProfit: String = if (profitOrLoss == Profit) totalDeductionsCaption(profitOrLoss) else totalAdditionsCaption(profitOrLoss)
    def deductionsTitleIfProfit: String   = if (profitOrLoss == Profit) deductionsCaption(profitOrLoss) else additionsCaption(profitOrLoss)
    val rows: Seq[SummaryListRow] = Seq(
      buildBigDecimalKeyValueRow("profitOrLoss.capitalAllowances", netBusinessProfitOrLossValues.capitalAllowances),
      buildBigDecimalKeyValueRow("profitOrLoss.turnoverNotTaxable", netBusinessProfitOrLossValues.turnoverNotTaxableAsBusinessProfit),
      buildBigDecimalKeyValueRow(deductionsCaptionIfProfit, netBusinessProfitOrLossValues.totalDeductions)
    )

    SummaryList(rows)
//    buildSummaryList(None, rows, caption = Some(messages(deductionsTitleIfProfit)), SummaryListClasses)
  }

}
