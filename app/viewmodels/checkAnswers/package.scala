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

import models.common.UserType
import pages.Page
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

package object checkAnswers {

  def buildRowBoolean(answer: Boolean,
                      callLink: Call,
                      keyMessage: String,
                      changeMessage: String,
                      rightTextAlign: Boolean = false,
                      flipKeyToValueWidthRatio: Boolean = false)(implicit messages: Messages): SummaryListRow = {
    val messageAnswer = if (answer) "site.yes" else "site.no"
    buildRowString(messages(messageAnswer), callLink, keyMessage, changeMessage, rightTextAlign, flipKeyToValueWidthRatio)
  }

  def mkBooleanSummary(answer: Boolean, callLink: Call, page: Page, userType: UserType)(implicit messages: Messages): SummaryListRow =
    buildRowBoolean(
      answer,
      callLink,
      messages(s"${page.pageName}.subHeading.cya.${userType}"),
      s"${page.pageName}.change.hidden",
      rightTextAlign = true
    )

  def buildRowBigDecimal(answer: BigDecimal, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow =
    buildRowString(s"£${formatMoney(answer)}", callLink, keyMessage, changeMessage, rightTextAlign = true)

  def mkBigDecimalSummary(answer: BigDecimal, callLink: Call, page: Page, userType: UserType)(implicit messages: Messages): SummaryListRow =
    buildRowBigDecimal(
      answer,
      callLink,
      messages(s"${page.pageName}.subHeading.cya.${userType}"),
      s"${page.pageName}.change.hidden"
    )

  def buildRowInt(answer: Int, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow =
    buildRowString(answer.toString, callLink, keyMessage, changeMessage, rightTextAlign = true)

  def buildRowString(answer: String,
                     callLink: Call,
                     keyMessage: String,
                     changeMessage: String,
                     rightTextAlign: Boolean = false,
                     flipKeyToValueWidthRatio: Boolean = false)(implicit messages: Messages): SummaryListRow = {
    val oneThirdWidth  = "govuk-!-width-one-third"
    val twoThirdsWidth = "govuk-!-width-two-thirds"
    val keyClasses     = if (flipKeyToValueWidthRatio) oneThirdWidth else twoThirdsWidth
    val valueClasses = s"${if (flipKeyToValueWidthRatio) twoThirdsWidth else oneThirdWidth}${if (rightTextAlign) " govuk-!-text-align-right" else ""}"
    SummaryListRowViewModel(
      key = Key(content = keyMessage, classes = keyClasses),
      value = Value(content = HtmlContent(answer), classes = valueClasses),
      actions = Seq(
        ActionItemViewModel("site.change", callLink.url)
          .withVisuallyHiddenText(messages(changeMessage))
      )
    )
  }

  def formatAnswer(answer: String)(implicit messages: Messages): String =
    answer match {
      case "no" | "false" => messages("site.no")
      case "yes" | "true" => messages("site.yes")
      case value          => messages(s"expenses.$value.cya")
    }
}
