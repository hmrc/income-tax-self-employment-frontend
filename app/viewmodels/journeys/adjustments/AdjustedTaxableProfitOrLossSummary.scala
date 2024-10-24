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

import models.common.{TaxYear, UserType}
import models.journeys.adjustments.ProfitOrLoss.{Loss, Profit}
import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow, buildTableRow}
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{additionsCaption, deductionsCaption}

case class AdjustedTaxableProfitOrLossSummary(adjustedProfitOrLossTable: Table,
                                              netProfitOrLossTable: Table,
                                              expensesTable: Table,
                                              capitalAllowanceTable: Table,
                                              adjustmentsTable: Table)
object AdjustedTaxableProfitOrLossSummary {

  val topMarginClass: String      = "govuk-!-margin-top-6"
  val doubleMarginClasses: String = s"$topMarginClass govuk-!-margin-bottom-9"

  def buildTables(adjustedTaxableProfitOrLoss: BigDecimal,
                  netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                  taxYear: TaxYear,
                  journeyIsProfitOrLoss: ProfitOrLoss,
                  userType: UserType)(implicit messages: Messages): AdjustedTaxableProfitOrLossSummary = {
    val goodsAndServicesForOwnUse = netBusinessProfitOrLossValues.goodsAndServicesForOwnUse
    val adjustments               = netBusinessProfitOrLossValues.outstandingBusinessIncome

    AdjustedTaxableProfitOrLossSummary(
      buildYourAdjustedProfitOrLossTable(adjustedTaxableProfitOrLoss, adjustments, netBusinessProfitOrLossValues, taxYear, journeyIsProfitOrLoss),
      NetBusinessProfitOrLossSummary.buildNetProfitOrLossTable(netBusinessProfitOrLossValues, journeyIsProfitOrLoss, doubleMarginClasses),
      NetBusinessProfitOrLossSummary.buildExpensesTable(
        netBusinessProfitOrLossValues,
        goodsAndServicesForOwnUse,
        journeyIsProfitOrLoss,
        userType,
        doubleMarginClasses),
      NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netBusinessProfitOrLossValues, journeyIsProfitOrLoss, topMarginClass),
      buildAdjustmentsTable(adjustments, topMarginClass)
    )
  }

  def buildYourAdjustedProfitOrLossTable(adjustedTaxableProfitOrLoss: BigDecimal,
                                         adjustments: BigDecimal,
                                         values: NetBusinessProfitOrLossValues,
                                         taxYear: TaxYear,
                                         profitOrLoss: ProfitOrLoss)(implicit messages: Messages): Table = {
    val startYear = taxYear.startYear.toString
    val endYear   = taxYear.toString
    val isProfit  = profitOrLoss == Profit

    def additionsOrDeductionsRow(returnAdditionsRow: Boolean) =
      if (returnAdditionsRow) buildTableAmountRow(additionsCaption(profitOrLoss), values.totalAdditions)
      else buildTableAmountRow(deductionsCaption(profitOrLoss), values.totalDeductions)

    val netProfitOrLossForTaxPurposesRow = {
      val amount                          = values.getNetBusinessProfitOrLossForTaxPurposes
      val formattedAmount                 = formatSumMoneyNoNegative(List(amount))
      val netForTaxPurposesIsProfitOrLoss = if (amount < 0) Loss else Profit
      buildTableRow(s"profitOrLossCalculation.adjustedTable.netForTaxPurposes.$netForTaxPurposesIsProfitOrLoss", formattedAmount)
    }

    val rows =
      List(
        buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", values.netProfitOrLossAmount),
        additionsOrDeductionsRow(returnAdditionsRow = isProfit),
        additionsOrDeductionsRow(returnAdditionsRow = !isProfit),
        netProfitOrLossForTaxPurposesRow,
        buildTableAmountRow("journeys.adjustments", adjustments),
        buildTableAmountRow(
          s"profitOrLossCalculation.adjustedTable.adjustedTaxableProfitOrLoss.$profitOrLoss",
          adjustedTaxableProfitOrLoss,
          classes = "govuk-!-font-weight-bold",
          optArgs = Seq(startYear, endYear)
        )
      )
    buildTable(None, rows)
  }

  def buildAdjustmentsTable(adjustments: BigDecimal, tableClasses: String)(implicit messages: Messages): Table = {
    val rows = Seq(
      buildTableAmountRow("adjustments.anyOtherBusinessIncome", adjustments),
      buildTableAmountRow("adjustments.totalAdjustments", adjustments)
    )
    buildTable(None, rows, caption = Some(messages("journeys.adjustments")), tableClasses)
  }
}
