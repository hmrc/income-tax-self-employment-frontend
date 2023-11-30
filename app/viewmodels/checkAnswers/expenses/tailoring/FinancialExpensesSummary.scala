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

import controllers.journeys.expenses.tailoring.routes.FinancialExpensesController
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.tailoring.FinancialExpensesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object FinancialExpensesSummary {

  def row()(implicit messages: Messages, answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType): Option[SummaryListRow] =
    answers.get(FinancialExpensesPage, Some(businessId)).map { answers =>
        SummaryListRowViewModel(
          key = Key(
            content = s"financialExpenses.subHeading.$userType",
            classes = "govuk-!-width-two-thirds"
          ),
          value = Value(
            content = formatFinancialExpensesAnswers(answers, userType),
            classes = "govuk-!-width-one-third"
          ),
        actions = Seq(
          ActionItemViewModel("site.change", FinancialExpensesController.onPageLoad(taxYear, businessId, CheckMode).url)
            .withVisuallyHiddenText(messages("financialExpenses.change.hidden"))
        )
      )
    }

}
