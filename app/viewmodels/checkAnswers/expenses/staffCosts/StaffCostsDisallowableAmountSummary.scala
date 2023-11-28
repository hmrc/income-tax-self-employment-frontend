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
import models.journeys.expenses.DisallowableStaffCosts
import models.journeys.expenses.DisallowableStaffCosts._
import pages.expenses.staffCosts._
import pages.expenses.tailoring.DisallowableStaffCostsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import utils.MoneyUtils.formatMoney
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object StaffCostsDisallowableAmountSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(DisallowableStaffCostsPage, Some(businessId.value))
      .filter(isDisallowable)
      .flatMap(_ => createSummaryListRow(answers, taxYear, businessId.value, userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: String, userType: UserType)(implicit messages: Messages) =
    for {
      disallowableAmount <- answers.get(StaffCostsDisallowableAmountPage, Some(businessId.value))
      allowableAmount    <- answers.get(StaffCostsAmountPage, Some(businessId.value))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"staffCostsDisallowableAmount.title.$userType", formatMoney(allowableAmount)),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = s"£${formatMoney(disallowableAmount)}",
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel("site.change", routes.StaffCostsDisallowableAmountController.onPageLoad(taxYear, BusinessId(businessId), CheckMode).url)
          .withVisuallyHiddenText(messages("staffCostsDisallowableAmount.change.hidden"))
      )
    )

  private def isDisallowable(disallowableStaffCosts: DisallowableStaffCosts): Boolean =
    disallowableStaffCosts match {
      case Yes => true
      case No  => false
    }

}
