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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.MoneyUtils.formatSumMoneyNoNegative
import viewmodels.checkAnswers.{buildBigDecimalKeyValueRow, buildKeyValueRow}
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{additionsCaption, deductionsCaption, marginBottomClass}

case class AdjustedTaxableProfitOrLossSummary(adjustedProfitOrLossSummaryList: SummaryList,
                                              netProfitOrLossSummaryList: SummaryList,
                                              expensesSummaryList: SummaryList,
                                              capitalAllowanceSummaryList: SummaryList,
                                              adjustmentsSummaryList: SummaryList)
object AdjustedTaxableProfitOrLossSummary {

  def buildSummaryLists(adjustedTaxableProfitOrLoss: BigDecimal,
                        netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                        taxYear: TaxYear,
                        journeyIsProfitOrLoss: ProfitOrLoss,
                        userType: UserType)(implicit messages: Messages): AdjustedTaxableProfitOrLossSummary = {
    val goodsAndServicesForOwnUse = netBusinessProfitOrLossValues.goodsAndServicesForOwnUse
    val adjustments               = netBusinessProfitOrLossValues.outstandingBusinessIncome

    AdjustedTaxableProfitOrLossSummary(
      buildYourAdjustedProfitOrLossSummaryList(
        adjustedTaxableProfitOrLoss,
        adjustments,
        netBusinessProfitOrLossValues,
        taxYear,
        journeyIsProfitOrLoss),
      NetBusinessProfitOrLossSummary.buildNetProfitOrLossSummaryList(netBusinessProfitOrLossValues, journeyIsProfitOrLoss, marginBottomClass),
      NetBusinessProfitOrLossSummary.buildExpensesSummaryList(
        netBusinessProfitOrLossValues,
        goodsAndServicesForOwnUse,
        journeyIsProfitOrLoss,
        userType,
        marginBottomClass),
      NetBusinessProfitOrLossSummary.buildCapitalAllowancesSummaryList(netBusinessProfitOrLossValues, journeyIsProfitOrLoss, ""),
      buildAdjustmentsSummaryList(adjustments, "")
    )
  }

  def buildYourAdjustedProfitOrLossSummaryList(adjustedTaxableProfitOrLoss: BigDecimal,
                                               adjustments: BigDecimal,
                                               values: NetBusinessProfitOrLossValues,
                                               taxYear: TaxYear,
                                               profitOrLoss: ProfitOrLoss)(implicit messages: Messages): SummaryList = {
    val startYear = taxYear.startYear.toString
    val endYear   = taxYear.toString
    val isProfit  = profitOrLoss == Profit

    def additionsOrDeductionsRow(returnAdditionsRow: Boolean) =
      if (returnAdditionsRow) buildBigDecimalKeyValueRow(additionsCaption(profitOrLoss), values.totalAdditions)
      else buildBigDecimalKeyValueRow(deductionsCaption(profitOrLoss), values.totalDeductions)

    val netProfitOrLossForTaxPurposesRow = {
      val amount                          = values.getNetBusinessProfitOrLossForTaxPurposes
      val formattedAmount                 = formatSumMoneyNoNegative(List(amount))
      val netForTaxPurposesIsProfitOrLoss = if (amount < 0) Loss else Profit
      buildKeyValueRow(s"profitOrLossCalculation.adjustedSummary.netForTaxPurposes.$netForTaxPurposesIsProfitOrLoss", formattedAmount)
    }
    val adjustedTaxableAmountIsProfitOrLoss = if (adjustedTaxableProfitOrLoss < 0) Loss else Profit

    val rows =
      List(
        buildBigDecimalKeyValueRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", values.netProfitOrLossAmount),
        additionsOrDeductionsRow(returnAdditionsRow = isProfit),
        additionsOrDeductionsRow(returnAdditionsRow = !isProfit),
        netProfitOrLossForTaxPurposesRow,
        buildBigDecimalKeyValueRow("journeys.adjustments", adjustments),
        buildKeyValueRow(
          s"profitOrLossCalculation.adjustedSummary.adjustedTaxableProfitOrLoss.$adjustedTaxableAmountIsProfitOrLoss",
          formatSumMoneyNoNegative(List(adjustedTaxableProfitOrLoss)),
          optKeyArgs = Seq(startYear, endYear),
          contentInBold = true
        )
      )
    SummaryList(rows)
  }

  def buildAdjustmentsSummaryList(adjustments: BigDecimal, classes: String)(implicit messages: Messages): SummaryList = {
    val rows = Seq(
      buildBigDecimalKeyValueRow("adjustments.anyOtherBusinessIncome", adjustments),
      buildBigDecimalKeyValueRow("adjustments.totalAdjustments", adjustments)
    )
    SummaryList(rows, classes = classes)
  }
}
