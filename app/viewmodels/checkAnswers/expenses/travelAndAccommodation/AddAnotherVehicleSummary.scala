/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses.travelAndAccommodation

import controllers.journeys.expenses.travelAndAccommodation.routes
import models.CheckMode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
import pages.expenses.travelAndAccommodation.TravelForWorkYourVehiclePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import viewmodels.journeys.SummaryListCYA

object AddAnotherVehicleSummary {

  def buildSummaryList(taxYear: TaxYear, businessId: BusinessId, answers: UserAnswers)(implicit messages: Messages): SummaryList =
    SummaryListCYA.summaryList(
      answers.get(TravelForWorkYourVehiclePage, businessId).toList.zipWithIndex.map { case (vehicleName, index) =>
        row(taxYear, index + 1, businessId, vehicleName)
      }
    )

  def row(taxYear: TaxYear, index: Int, businessId: BusinessId, vehicleName: String)(implicit messages: Messages): SummaryListRow = {
    val value = ValueViewModel(
      HtmlContent(
        HtmlFormat.escape(vehicleName)
      )
    )

    SummaryListRowViewModel(
      key = s"Vehicle $index",
      value = value,
      actions = Seq(
        ActionItemViewModel("site.change", routes.VehicleTypeController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages(s"vehicleType.change.hidden", index)),
        ActionItemViewModel("site.remove", routes.VehicleTypeController.onPageLoad(taxYear, businessId, CheckMode).url)
          .withVisuallyHiddenText(messages(s"vehicleType.remove.hidden", index))
      )
    )
  }

}
