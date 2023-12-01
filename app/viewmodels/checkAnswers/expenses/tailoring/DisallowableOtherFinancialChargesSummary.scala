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

package viewmodels.checkAnswers.expenses.tailoring

import controllers.journeys.expenses.tailoring.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.FinancialExpenses.OtherFinancialCharges
import pages.expenses.tailoring.{DisallowableOtherFinancialChargesPage, FinancialExpensesPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DisallowableOtherFinancialChargesSummary {

  def row()(implicit messages: Messages, answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType): Option[SummaryListRow] =
    answers
      .get(FinancialExpensesPage, Some(businessId))
      .filter(_ == OtherFinancialCharges)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    answers.get(DisallowableOtherFinancialChargesPage, Some(businessId)).map { answer =>
      SummaryListRowViewModel(
        key = Key(
          content = s"disallowableOtherFinancialCharges.title.$userType",
          classes = "govuk-!-width-two-thirds"
        ),
        value = Value(
          content = formatAnswer(answer.toString),
          classes = "govuk-!-width-one-third"
        ),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DisallowableOtherFinancialChargesController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("disallowableOtherFinancialCharges.change.hidden"))
        )
      )
    }

}
