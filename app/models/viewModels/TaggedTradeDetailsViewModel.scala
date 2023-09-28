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

package models.viewModels

import models.requests.TaggedTradeDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow, Value}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

case class TaggedTradeDetailsViewModel(tradingName: String,
                                       businessId: String,
                                       statusList: SummaryList)

object TaggedTradeDetailsViewModel {

  private val completedStatus = "completed"
  private val inProgressStatus = "inProgress"
  private val notStartedStatus = "notStarted"
  private val cannotStartYetStatus = "cannotStartYet"

  def buildSummaryList(business: TaggedTradeDetails)(implicit messages: Messages): SummaryList = {

    SummaryList(
      rows = Seq(
        row("selfEmploymentAbroad", business.abroadStatus),
        row("income", sortStatuses(business.incomeStatus, business.abroadStatus)),
        row("expensesCategories", sortStatuses(business.expensesStatus, business.incomeStatus)),
        row("nationalInsurance", sortStatuses(business.nationalInsuranceStatus, business.expensesStatus))
      ),
      classes = "govuk-!-margin-bottom-7")
  }

  private def row(rowKey: String, status: String)
                 (implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = Key(
        content = s"common.$rowKey",
        classes = "govuk-!-font-weight-regular"
      ),
      value = Value(),
      actions = Seq(ActionItemViewModel((s"status.$status"), "#"))
    )
  }

  private def sortStatuses(status: String, priorStatus: String): String =
    if (priorStatus.equals(notStartedStatus)) cannotStartYetStatus else status
}
