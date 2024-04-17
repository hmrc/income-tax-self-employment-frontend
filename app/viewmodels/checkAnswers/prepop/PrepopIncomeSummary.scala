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
import models.journeys.income.IncomePrepopAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import utils.MoneyUtils
import viewmodels.checkAnswers.buildTableAmountRow

object PrepopIncomeSummary extends MoneyUtils {

  def headRow(implicit messages: Messages): Option[Seq[HeadCell]] =
    Seq(
      HeadCell(HtmlContent(messages("income.income"))),
      HeadCell(HtmlContent(messages("site.amount")), classes = "govuk-!-text-align-right")
    ).some

  def turnoverIncomeRow(answers: IncomePrepopAnswers)(implicit messages: Messages): Option[Seq[TableRow]] =
    answers.turnoverIncome.map(buildTableAmountRow("site.sales", _))

  def otherIncomeRow(answers: IncomePrepopAnswers)(implicit messages: Messages): Option[Seq[TableRow]] =
    answers.otherIncome.map(buildTableAmountRow("income.otherBusinessIncome", _))

  def totalIncomeRow(answers: IncomePrepopAnswers)(implicit messages: Messages): Option[Seq[TableRow]] =
    buildTableAmountRow("income.totalIncome", answers.totalIncome, classes = "govuk-!-font-weight-bold").some
}
