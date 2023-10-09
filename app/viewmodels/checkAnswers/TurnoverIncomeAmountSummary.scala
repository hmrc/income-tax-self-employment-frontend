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

package viewmodels.checkAnswers

import controllers.journeys.income.routes.TurnoverNotTaxableController
import models.{CheckMode, UserAnswers}
import pages.TurnoverIncomeAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TurnoverIncomeAmountSummary {

  def row(answers: UserAnswers, taxYear: Int)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TurnoverIncomeAmountPage).map { answer =>
      SummaryListRowViewModel(
        key = "turnoverIncomeAmount.checkYourAnswersLabel",
        value = ValueViewModel(answer.toString),
        actions = Seq(
          ActionItemViewModel("site.change", TurnoverNotTaxableController.onPageLoad(taxYear, CheckMode).url)
            .withVisuallyHiddenText(messages("turnoverIncomeAmount.change.hidden"))
        )
      )
    }

}
