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
import models.journeys.expenses.ExpensesTailoring.NoExpenses
import pages.expenses.tailoring.ExpensesCategoriesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object ExpensesTailoringSummary {
  def row(rightTextAlign: Boolean = false)(implicit
      messages: Messages,
      answers: UserAnswers,
      taxYear: TaxYear,
      businessId: BusinessId,
      userType: UserType): Option[SummaryListRow] =
    answers.get(ExpensesCategoriesPage, Some(businessId)).map { answer =>
      val optUserType = if (answer == NoExpenses) s".$userType" else ""
      buildRowString(
        messages(s"expenses.$answer$optUserType"),
        routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, CheckMode),
        s"expenses.cyaSummary.$userType",
        "expensesCategories.change.hidden",
        rightTextAlign
      )
    }

}
