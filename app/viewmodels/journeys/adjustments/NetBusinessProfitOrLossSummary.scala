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
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

case class NetBusinessProfitOrLossSummary(netProfitLossTable: Table, expensesTable: Table, capitalAllowancesTable: Table)

object NetBusinessProfitOrLossSummary {

  def totalAdditionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOrLoss.totalAdditions.$profitOrLoss"
  def totalDeductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOrLoss.totalDeductions.$profitOrLoss"
  def additionsCaption(profitOrLoss: ProfitOrLoss)  = s"profitOrLoss.additions.$profitOrLoss"
  def deductionsCaption(profitOrLoss: ProfitOrLoss) = s"profitOrLoss.deductions.$profitOrLoss"

  def buildTables(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                  userAnswers: UserAnswers,
                  profitOrLoss: ProfitOrLoss,
                  userType: UserType,
                  businessId: BusinessId)(implicit messages: Messages): NetBusinessProfitOrLossSummary = {
    val goodsAndServicesForOwnUse: BigDecimal = userAnswers.get(GoodsAndServicesAmountPage, businessId).getOrElse(0)
    NetBusinessProfitOrLossSummary(
      buildNetProfitOrLossTable(netBusinessProfitOrLossValues, profitOrLoss, "govuk-!-margin-top-6 govuk-!-margin-bottom-9"),
      buildExpensesTable(netBusinessProfitOrLossValues, goodsAndServicesForOwnUse, profitOrLoss, userType, "govuk-!-margin-bottom-9"),
      buildCapitalAllowancesTable(netBusinessProfitOrLossValues, profitOrLoss, "")
    )
  }

  def buildNetProfitOrLossTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss, tableClasses: String)(implicit
                                                                                                                                                messages: Messages): Table = {

    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.turnover", netBusinessProfitOrLossValues.turnover),
      buildTableAmountRow("incomeNotCountedAsTurnover.title", netBusinessProfitOrLossValues.incomeNotCountedAsTurnover),
      buildTableAmountRow("profitOrLoss.totalExpenses", netBusinessProfitOrLossValues.totalExpenses),
      buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", netBusinessProfitOrLossValues.netProfitOrLossAmount)
    )

    buildTable(None, rows, caption = Some(messages(s"profitOrLoss.netProfitOrLoss.$profitOrLoss")), tableClasses)
  }

  def buildExpensesTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                         goodsAndServicesForOwnUse: BigDecimal,
                         profitOrLoss: ProfitOrLoss,
                         userType: UserType,
                         tableClasses: String)(implicit messages: Messages): Table = {
    def additionsCaptionIfProfit: String = if (profitOrLoss == Profit) totalAdditionsCaption(profitOrLoss) else totalDeductionsCaption(profitOrLoss)
    def additionsTitleIfProfit: String = if (profitOrLoss == Profit) additionsCaption(profitOrLoss) else deductionsCaption(profitOrLoss)
    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("selectCapitalAllowances.balancingCharge", netBusinessProfitOrLossValues.balancingCharge),
      buildTableAmountRow(s"goodsAndServicesForYourOwnUse.title.$userType", goodsAndServicesForOwnUse),
      buildTableAmountRow("profitOrLoss.disallowableExpenses", netBusinessProfitOrLossValues.disallowableExpenses),
      buildTableAmountRow(additionsCaptionIfProfit, netBusinessProfitOrLossValues.totalAdditions)
    )

    buildTable(None, rows, caption = Some(messages(additionsTitleIfProfit)), tableClasses)
  }

  def buildCapitalAllowancesTable(netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues, profitOrLoss: ProfitOrLoss, tableClasses: String)(
      implicit messages: Messages): Table = {
    def deductionsCaptionIfProfit: String = if (profitOrLoss == Profit) totalDeductionsCaption(profitOrLoss) else totalAdditionsCaption(profitOrLoss)
    def deductionsTitleIfProfit: String = if (profitOrLoss == Profit) deductionsCaption(profitOrLoss) else additionsCaption(profitOrLoss)
    val rows: Seq[Seq[TableRow]] = Seq(
      buildTableAmountRow("profitOrLoss.capitalAllowances", netBusinessProfitOrLossValues.capitalAllowances),
      buildTableAmountRow("profitOrLoss.turnoverNotTaxable", netBusinessProfitOrLossValues.turnoverNotTaxableAsBusinessProfit),
      buildTableAmountRow(deductionsCaptionIfProfit, netBusinessProfitOrLossValues.totalDeductions)
    )

    buildTable(None, rows, caption = Some(messages(deductionsTitleIfProfit)), tableClasses)
  }

}
