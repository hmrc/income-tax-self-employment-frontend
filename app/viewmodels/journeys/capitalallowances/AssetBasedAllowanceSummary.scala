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

import models.journeys.adjustments.NetBusinessProfitOrLossValues
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.{buildBigDecimalKeyValueRow, buildKeyValueRow}

object AssetBasedAllowanceSummary {

  def buildNetProfitOrLossSummaryList(answers: NetBusinessProfitOrLossValues)(implicit messages: Messages): SummaryList = {
    val rows = Seq(
      buildBigDecimalKeyValueRow("profitOrLoss.turnover", answers.turnover),
      buildBigDecimalKeyValueRow("profitOrLoss.incomeNotCountedAsTurnover", answers.incomeNotCountedAsTurnover),
      buildKeyValueRow("profitOrLoss.totalExpenses", s"(£${answers.totalExpenses})"),
      buildBigDecimalKeyValueRow(
        s"profitOrLoss.netProfitOrLoss.${answers.netProfitOrLoss}",
        answers.netProfitOrLossAmount,
        classes = "govuk-!-font-weight-bold")
    )

    SummaryList(rows)
  }
}
