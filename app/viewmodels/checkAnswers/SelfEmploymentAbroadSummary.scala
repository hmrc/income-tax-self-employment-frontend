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

import models.{CheckMode, UserAnswers}
import pages.SelfEmploymentAbroadPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SelfEmploymentAbroadSummary {

  def row(taxYear: Int, isAgent: Boolean, userAnswers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    userAnswers.get(SelfEmploymentAbroadPage) match {
      case Some(answer) =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = Key(
            content = s"selfEmploymentAbroad.checkYourAnswersLabel.${if (isAgent) "agent" else "individual"}",
            classes = "govuk-!-width-two-thirds"),
          value = Value(content = value, classes = "govuk-!-width-one-third"),
          actions = Seq(
            ActionItemViewModel("site.change", controllers.journeys.abroad.routes.SelfEmploymentAbroadController.onPageLoad(taxYear, CheckMode).url)
              .withVisuallyHiddenText(messages("selfEmploymentAbroad.change.hidden"))
          )
        )
      case None => throw new RuntimeException("No UserAnswers retrieved for SelfEmploymentAbroadPage")
    }
  }

}
