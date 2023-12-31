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

package viewmodels

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

package object checkAnswers {

  def buildRowBoolean(answer: Boolean, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow = {
    val value = if (answer) "site.yes" else "site.no"
    SummaryListRowViewModel(
      key = Key(content = keyMessage, classes = "govuk-!-width-two-thirds"),
      value = Value(content = value, classes = "govuk-!-width-one-third"),
      actions = Seq(
        ActionItemViewModel("site.change", callLink.url)
          .withVisuallyHiddenText(messages(changeMessage))
      )
    )
  }

  def buildRowBigDecimal(answer: BigDecimal, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key(content = keyMessage, classes = "govuk-!-width-two-thirds"),
      value = Value(content = s"£${formatMoney(answer)}", classes = "govuk-!-width-one-third"),
      actions = Seq(
        ActionItemViewModel("site.change", callLink.url)
          .withVisuallyHiddenText(messages(changeMessage))
      )
    )

  def buildRowString(answer: String, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key(content = keyMessage, classes = "govuk-!-width-two-thirds"),
      value = Value(content = answer, classes = "govuk-!-width-one-third"),
      actions = Seq(
        ActionItemViewModel("site.change", callLink.url)
          .withVisuallyHiddenText(messages(changeMessage))
      )
    )
}
