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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SelfEmploymentDetailsViewModel {

  def row(answers: UserAnswers, rowKey: String, rowContent: String, userIsAgent: Option[Boolean] = None)(implicit messages: Messages): SummaryListRow = {
    val agentIndividual = userIsAgent match {
      case None => ""
      case Some(isAgent) => if (isAgent) ".agent" else ".individual"
    }
    SummaryListRowViewModel(
      key = Key(
        content = s"checkYourSelfEmploymentDetails.$rowKey$agentIndividual",
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = rowContent,
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(ActionItemViewModel(messages("site.change"), "#"))
    )
  }

}
