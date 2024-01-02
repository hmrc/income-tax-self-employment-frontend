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

package viewmodels.checkAnswers.expenses.otherExpenses

import controllers.journeys.expenses.otherExpenses.routes._
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.individualCategories.OtherExpenses
import models.journeys.expenses.individualCategories.OtherExpenses.YesDisallowable
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.ImplicitConversions
import viewmodels.govuk.summarylist._

object OtherExpensesDisallowableAmountSummary extends ImplicitConversions {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(OtherExpensesPage, Some(businessId))
      .filter(areOtherExpensesDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      amount             <- answers.get(OtherExpensesAmountPage, Some(businessId))
      disallowableAmount <- answers.get(OtherExpensesDisallowableAmountPage, Some(businessId))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"otherExpensesDisallowableAmount.title.$userType", formatMoney(amount)),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = messages(s"£${formatMoney(disallowableAmount)}"),
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel(messages("site.change"), OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages("otherExpensesDisallowableAmount.change.hidden"))
      )
    )

  private def areOtherExpensesDisallowable(otherExpenses: OtherExpenses): Boolean =
    otherExpenses match {
      case YesDisallowable => true
      case _               => false
    }

}
