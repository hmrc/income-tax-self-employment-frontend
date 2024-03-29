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

package viewmodels.checkAnswers.expenses.staffCosts

import controllers.journeys.expenses.staffCosts.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.expenses.staffCosts._
import pages.expenses.tailoring.individualCategories.DisallowableStaffCostsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.buildRowBigDecimal

object StaffCostsDisallowableAmountSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableStaffCostsPage, Some(businessId))
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages) =
    for {
      disallowableAmount <- answers.get(StaffCostsDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(StaffCostsAmountPage, Some(businessId))
    } yield buildRowBigDecimal(
      disallowableAmount,
      routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode),
      messages(s"staffCostsDisallowableAmount.title.$userType", formatMoney(allowableAmount)),
      "staffCostsDisallowableAmount.change.hidden"
    )

}
