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
import models.journeys.adjustments.ProfitOrLoss.Profit
import models.journeys.adjustments.{NetBusinessProfitOrLossValues, ProfitOrLoss}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}
import viewmodels.journeys.adjustments.NetBusinessProfitOrLossSummary.{additionsCaption, deductionsCaption, totalAdditionsCaption, totalDeductionsCaption}

case class AdjustedTaxableProfitOrLossSummary(adjustedProfitOrLossTable: Table,
                                              netProfitOrLossTable: Table,
                                              expensesTable: Table,
                                              capitalAllowanceTable: Table,
                                              adjustmentsTable: Table)
object AdjustedTaxableProfitOrLossSummary {

  private val topMarginClass      = "govuk-!-margin-top-6"
  private val doubleMarginClasses = s"$topMarginClass govuk-!-margin-bottom-9"

  def buildTables(taxableProfitOrLoss: BigDecimal,
                  netBusinessProfitOrLossValues: NetBusinessProfitOrLossValues,
                  taxYear: TaxYear,
                  profitOrLoss: ProfitOrLoss,
                  userType: UserType)(implicit messages: Messages): AdjustedTaxableProfitOrLossSummary = {
    val goodsAndServicesForOwnUse = netBusinessProfitOrLossValues.goodsAndServicesForOwnUse
    val adjustments               = netBusinessProfitOrLossValues.outstandingBusinessIncome

    AdjustedTaxableProfitOrLossSummary(
      buildYourAdjustedProfitOrLossTable(taxableProfitOrLoss, adjustments, netBusinessProfitOrLossValues, taxYear, profitOrLoss),
      NetBusinessProfitOrLossSummary.buildNetProfitOrLossTable(netBusinessProfitOrLossValues, profitOrLoss, doubleMarginClasses),
      NetBusinessProfitOrLossSummary.buildExpensesTable(
        netBusinessProfitOrLossValues,
        goodsAndServicesForOwnUse,
        profitOrLoss,
        userType,
        doubleMarginClasses),
      NetBusinessProfitOrLossSummary.buildCapitalAllowancesTable(netBusinessProfitOrLossValues, profitOrLoss, topMarginClass),
      buildAdjustmentsTable(adjustments, topMarginClass)
    )
  }

  def buildYourAdjustedProfitOrLossTable(taxableProfitOrLoss: BigDecimal,
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

    val rows =
      List(
        buildTableAmountRow(s"profitOrLoss.netProfitOrLoss.$profitOrLoss", values.netProfitOrLossAmount),
        additionsOrDeductionsRow(returnAdditionsRow = isProfit),
        additionsOrDeductionsRow(returnAdditionsRow = !isProfit),
        buildTableAmountRow(s"profitOrLossCalculation.adjustedTable.netForTaxPurposes.$profitOrLoss", values.turnoverNotTaxableAsBusinessProfit),
        buildTableAmountRow("journeys.adjustments", adjustments),
        buildTableAmountRow(
          s"profitOrLossCalculation.adjustedTable.adjustedTaxableProfitOrLoss.$profitOrLoss",
          taxableProfitOrLoss,
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
