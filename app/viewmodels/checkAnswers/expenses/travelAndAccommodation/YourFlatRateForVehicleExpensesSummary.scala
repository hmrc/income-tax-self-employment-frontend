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
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses
import pages.CostsNotCoveredPage
import pages.expenses.travelAndAccommodation.{TravelForWorkYourMileagePage, YourFlatRateForVehicleExpensesPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.MoneyUtils.formatMoney
import viewmodels.checkAnswers.{buildRowString, formatAnswer}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object YourFlatRateForVehicleExpensesSummary {

  def row(taxYear: TaxYear, businessId: BusinessId, answers: UserAnswers, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] =
    (for {
      workMileage <- answers.get(TravelForWorkYourMileagePage, businessId)
      answer      <- answers.get(YourFlatRateForVehicleExpensesPage, businessId)
    } yield {
      val flatRateCalc = TravelMileageSummaryViewModel.totalFlatRateExpense(workMileage)

      answer match {
        case YourFlatRateForVehicleExpenses.Flatrate =>
          Some(
            buildRowString(
              messages("expenses.flatRate", flatRateCalc),
              callLink = routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, CheckMode),
              keyMessage = messages(s"yourFlatRateForVehicleExpenses.legend.$userType", flatRateCalc),
              changeMessage = "yourFlatRateForVehicleExpenses.change.hidden",
              rightTextAlign = true
            ))

        case YourFlatRateForVehicleExpenses.Actualcost =>
          Some(
            buildRowString(
              messages("expenses.actualCosts"),
              callLink = routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, CheckMode),
              keyMessage = messages(s"yourFlatRateForVehicleExpenses.legend.$userType", flatRateCalc),
              changeMessage = s"yourFlatRateForVehicleExpenses.change.hidden",
              rightTextAlign = true
            ))
      }
    }).flatten
}
