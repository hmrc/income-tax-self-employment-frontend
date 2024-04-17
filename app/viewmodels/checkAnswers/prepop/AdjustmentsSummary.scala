/*
 * Copyright 2023 HM Revenue & Customs
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

package viewmodels.checkAnswers.prepop

import cats.implicits.catsSyntaxOptionId
import models.journeys.adjustments.AdjustmentsPrepopAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table}
import utils.MoneyUtils
import viewmodels.checkAnswers.{buildTable, buildTableAmountRow}

object AdjustmentsSummary extends MoneyUtils {

  private def headRow(implicit messages: Messages): Option[Seq[HeadCell]] =
    Seq(
      HeadCell(HtmlContent(messages("adjustments.adjustment"))),
      HeadCell(HtmlContent(messages("site.amount")), classes = "govuk-!-text-align-right")
    ).some

  def buildAdjustmentsTable(answers: AdjustmentsPrepopAnswers)(implicit messages: Messages): Table = {
    val rows = Seq(
      answers.includedNonTaxableProfits.map(buildTableAmountRow("adjustments.includedNonTaxableProfits", _)),
      answers.accountingAdjustment.map(buildTableAmountRow("adjustments.accountingAdjustment", _)),
      answers.averagingAdjustment.map(buildTableAmountRow("adjustments.averagingAdjustment", _)),
      answers.outstandingBusinessIncome.map(buildTableAmountRow("adjustments.outstandingBusinessIncome", _)),
      answers.balancingChargeOther.map(buildTableAmountRow("adjustments.balancingChargeOther", _)),
      answers.goodsAndServicesOwnUse.map(buildTableAmountRow("adjustments.goodsAndServicesOwnUse", _)),
      answers.transitionProfitAmount.map(buildTableAmountRow("adjustments.transitionProfitAmount", _)),
      answers.transitionProfitAccelerationAmount.map(buildTableAmountRow("adjustments.transitionProfitAccelerationAmount", _))
    ).flatten
    buildTable(headRow, rows)
  }
}
