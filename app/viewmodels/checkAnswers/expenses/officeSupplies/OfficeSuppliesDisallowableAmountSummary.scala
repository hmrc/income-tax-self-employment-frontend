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

package viewmodels.checkAnswers.expenses.officeSupplies

import controllers.journeys.expenses.officeSupplies.routes.OfficeSuppliesDisallowableAmountController
import models.CheckMode
import models.database.UserAnswers
import models.journeys.expenses.OfficeSupplies
import models.journeys.expenses.OfficeSupplies.YesDisallowable
import pages.expenses.officeSupplies.{OfficeSuppliesAmountPage, OfficeSuppliesDisallowableAmountPage}
import pages.expenses.tailoring.OfficeSuppliesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object OfficeSuppliesDisallowableAmountSummary extends MoneyUtils {

  def row(answers: UserAnswers, taxYear: Int, businessId: String, authUserType: String)(implicit messages: Messages): Option[SummaryListRow] = {
    for {
      officeSupplies <- answers.get(OfficeSuppliesPage, Some(businessId))
      if areAnyOfficeSuppliesDisallowable(officeSupplies)
      disallowableAmount <- answers.get(OfficeSuppliesDisallowableAmountPage, Some(businessId))
      allowableAmount    <- answers.get(OfficeSuppliesAmountPage, Some(businessId))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"officeSuppliesDisallowableAmount.title.$authUserType", formatMoney(allowableAmount)),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = s"£${formatMoney(disallowableAmount)}",
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel("site.change", OfficeSuppliesDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages("officeSuppliesDisallowableAmount.change.hidden"))
      )
    )
  }

  private def areAnyOfficeSuppliesDisallowable(officeSupplies: OfficeSupplies): Boolean =
    officeSupplies match {
      case YesDisallowable => true
      case _               => false
    }

}
