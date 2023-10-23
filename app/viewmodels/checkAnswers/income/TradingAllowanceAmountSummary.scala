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

package viewmodels.checkAnswers.income

import controllers.journeys.income.routes.TradingAllowanceAmountController
import models.{CheckMode, UserAnswers}
import pages.income.TradingAllowanceAmountPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TradingAllowanceAmountSummary {

  def row(answers: UserAnswers, taxYear: Int, authUserType: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TradingAllowanceAmountPage).map { answer =>
      SummaryListRowViewModel(
        key = Key(content = s"tradingAllowanceAmount.checkYourAnswersLabel.$authUserType", classes = "govuk-!-width-two-thirds"),
        value = Value(content = s"£${answer.setScale(2)}", classes = "govuk-!-width-one-third"),
        actions = Seq(
          ActionItemViewModel("site.change", TradingAllowanceAmountController.onPageLoad(taxYear, CheckMode).url)
            .withVisuallyHiddenText(messages("TradingAllowanceAmount.change.hidden"))
        )
      )
    }

}
