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
import models.database.UserAnswers
import pages.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsCYAPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RepairsAndMaintenanceCostsCYASummary {

  def row(answers: UserAnswers, taxYear: Int, businessId: String)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(RepairsAndMaintenanceCostsCYAPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "repairsAndMaintenanceCostsCYA.checkYourAnswersLabel",
          value   = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.RepairsAndMaintenanceCostsCYAController.onPageLoad(taxYear, businessId).url)
              .withVisuallyHiddenText(messages("repairsAndMaintenanceCostsCYA.change.hidden"))
          )
        )
    }
}