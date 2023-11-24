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

package viewmodels.checkAnswers.expenses.repairsandmaintenance

import controllers.journeys.expenses.repairsandmaintenance.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.RepairsAndMaintenance
import models.journeys.expenses.RepairsAndMaintenance._
import models.requests.DataRequest
import pages.expenses.repairsandmaintenance.{RepairsAndMaintenanceAmountPage, RepairsAndMaintenanceDisallowableAmountPage}
import pages.expenses.tailoring.RepairsAndMaintenancePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow, Value}
import utils.MoneyUtils.formatMoney
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RepairsAndMaintenanceDisallowableAmountSummary {

  def row(request: DataRequest[_], taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    request.userAnswers
      .get(RepairsAndMaintenancePage, Some(businessId.value))
      .filter(areAnyDisallowable)
      .flatMap(_ => createSummaryListRow(request.userAnswers, taxYear, businessId, request.userType))

  private def createSummaryListRow(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId, userType: UserType)(implicit
      messages: Messages): Option[SummaryListRow] =
    for {
      disallowableAmount <- answers.get(RepairsAndMaintenanceDisallowableAmountPage, Some(businessId.value))
      allowableAmount    <- answers.get(RepairsAndMaintenanceAmountPage, Some(businessId.value))
    } yield SummaryListRowViewModel(
      key = Key(
        content = messages(s"repairsAndMaintenanceDisallowableAmount.title.$userType", formatMoney(allowableAmount)),
        classes = "govuk-!-width-two-thirds"
      ),
      value = Value(
        content = s"£${formatMoney(disallowableAmount)}",
        classes = "govuk-!-width-one-third"
      ),
      actions = Seq(
        ActionItemViewModel("site.change", routes.RepairsAndMaintenanceDisallowableAmountController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages("repairsAndMaintenanceDisallowableAmount.change.hidden"))
      )
    )

  private def areAnyDisallowable(repairsAndMaintenance: RepairsAndMaintenance): Boolean =
    repairsAndMaintenance match {
      case YesDisallowable   => true
      case YesAllowable | No => false
    }

}
