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
import pages.expenses.travelAndAccommodation.{TravelForWorkYourVehiclePage, VehicleTypePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.buildRowString

object VehicleTypeSummary {

  def row(answers: UserAnswers, taxYear: TaxYear, businessId: BusinessId)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(VehicleTypePage, businessId)
      .map { answer =>
        val vehicleName = answers.get(TravelForWorkYourVehiclePage, businessId).get

        buildRowString(
          answer = messages(s"vehicleType.$answer"),
          callLink = routes.VehicleTypeController.onPageLoad(taxYear, businessId, CheckMode),
          keyMessage = messages("vehicleType.heading", vehicleName),
          changeMessage = messages("vehicleType.change.hidden", vehicleName),
          rightTextAlign = true
        )
      }
}
