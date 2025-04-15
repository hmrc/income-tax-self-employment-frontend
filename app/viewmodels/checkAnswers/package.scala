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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import utils.MoneyUtils.{formatMoney, formatPosNegMoneyWithPounds}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

  def mkBooleanSummary(answer: Boolean,
                       callLink: Call,
                       page: Page,
                       userType: UserType,
                       rightTextAlign: Boolean,
                       overrideKeyMessage: Option[String] = None,
                       overrideChangeMessage: Option[String] = None)(implicit messages: Messages): SummaryListRow =
    buildRowBoolean(
      answer,
      callLink,
      overrideKeyMessage.fold(messages(s"${page.pageName}.subHeading.cya.$userType"))(newKey => newKey),
      overrideChangeMessage.fold(messages(s"${page.pageName}.change.hidden"))(newKey => newKey),
      rightTextAlign
    )

  def buildRowBigDecimal(answer: BigDecimal, callLink: Call, keyMessage: String, changeMessage: String, rightTextAlign: Boolean = true)(implicit
      messages: Messages): SummaryListRow =
    buildRowString(s"£${formatMoney(answer, addDecimalForWholeNumbers = false)}", callLink, keyMessage, changeMessage, rightTextAlign)

  def mkBigDecimalSummary(answer: BigDecimal,
                          callLink: Call,
                          page: Page,
                          userType: UserType,
                          rightTextAlign: Boolean,
                          overrideKeyMessage: Option[String] = None,
                          overrideChangeMessage: Option[String] = None)(implicit messages: Messages): SummaryListRow =
    buildRowBigDecimal(
      answer,
      callLink,
      overrideKeyMessage.fold(messages(s"${page.pageName}.subHeading.cya.$userType"))(newKey => newKey),
      overrideChangeMessage.fold(messages(s"${page.pageName}.change.hidden"))(newKey => newKey),
      rightTextAlign
    )

  def buildRowInt(answer: Int, callLink: Call, keyMessage: String, changeMessage: String)(implicit messages: Messages): SummaryListRow =
    buildRowString(answer.toString, callLink, keyMessage, changeMessage, rightTextAlign = true)

  def buildRowLocalDate(answer: LocalDate, callLink: Call, keyMessage: String, changeMessage: String, rightTextAlign: Boolean = false)(implicit
      messages: Messages): SummaryListRow =
    buildRowString(formatDate(answer), callLink, keyMessage, changeMessage, rightTextAlign)

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

  def buildChangeRemoveRow(answer: String, keyMessage: String, changeLink: Call, changeMessage: String, removeLink: Call, removeMessage: String)(
      implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = Key(
        content = keyMessage,
        classes = "govuk-!-font-weight-regular hmrc-summary-list__key"
      ),
      value = Value(
        content = HtmlContent(answer),
        classes = "govuk-!-font-weight-regular hmrc-summary-list__key"
      ),
      actions = Actions(
        classes = "govuk-summary-list__actions hmrc-summary-list__actions",
        items = Seq(
          ActionItemViewModel("site.change", changeLink.url)
            .withVisuallyHiddenText(messages(changeMessage)),
          ActionItemViewModel("site.remove", removeLink.url)
            .withVisuallyHiddenText(messages(removeMessage))
        )
      )
    )

  def buildBigDecimalKeyValueRow(keyMessage: String,
                                 value: BigDecimal,
                                 classes: String = "",
                                 optKeyArgs: Seq[String] = Seq.empty,
                                 optValueArgs: Seq[String] = Seq.empty,
                                 contentInBold: Boolean = false)(implicit messages: Messages): SummaryListRow =
    buildKeyValueRow(keyMessage, formatPosNegMoneyWithPounds(value), classes, optKeyArgs, optValueArgs, contentInBold)

  def buildKeyValueRow(keyMessage: String,
                       value: String,
                       classes: String = "",
                       optKeyArgs: Seq[String] = Seq.empty,
                       optValueArgs: Seq[String] = Seq.empty,
                       contentInBold: Boolean = false)(implicit messages: Messages): SummaryListRow = {
    val fontWeight = if (contentInBold) "govuk-!-font-weight-bold" else "govuk-!-font-weight-regular"
    SummaryListRowViewModel(
      key = Key(
        content = HtmlContent(messages(keyMessage, optKeyArgs: _*)),
        classes = s"$fontWeight hmrc-summary-list__key"
      ),
      value = Value(
        content = HtmlContent(messages(value, optValueArgs: _*)),
        classes = s"$fontWeight hmrc-summary-list__key govuk-!-text-align-right"
      )
    ).withCssClass(classes)
  }

  def buildTableAmountRow(key: String, answer: BigDecimal, classes: String = "", optArgs: Seq[String] = Seq.empty)(implicit
      messages: Messages): Seq[TableRow] =
    Seq(
      TableRow(content = HtmlContent(messages(key, optArgs: _*)), classes = classes),
      TableRow(content = HtmlContent(formatPosNegMoneyWithPounds(answer)), classes = s"govuk-!-text-align-right $classes")
    )

  def buildTable(headRow: Option[Seq[HeadCell]], rows: Seq[Seq[TableRow]], caption: Option[String] = None, tableClasses: String = ""): Table =
    Table(rows, headRow, caption, classes = tableClasses)

  def formatAnswer(answer: String)(implicit messages: Messages): String =
    answer match {
      case "no" | "false" => messages("site.no")
      case "yes" | "true" => messages("site.yes")
      case value          => messages(s"expenses.$value.cya")
    }

  def formatDate(date: LocalDate): String = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }
}
