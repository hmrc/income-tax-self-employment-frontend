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

  def row(taxYear: TaxYear, businessId: BusinessId, answers: UserAnswers, userType: UserType)(implicit messages: Messages): Option[SummaryListRow] = {

    val getAnswer = answers.get(YourFlatRateForVehicleExpensesPage)

    val workMileage = answers.get(TravelForWorkYourMileagePage, businessId).get

    val flatRateCalc = TravelMileageSummaryViewModel.totalFlatRateExpense(workMileage)

    val selectMessage = getAnswer match {
      case Some(YourFlatRateForVehicleExpenses.Flatrate) =>
        messages(s"expenses.flatRate", flatRateCalc)
      case Some(YourFlatRateForVehicleExpenses.Actualcost) =>
        messages("expenses.actualCosts")
    }

    Some(
      buildRowString(
        selectMessage,
        callLink = routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, CheckMode),
        keyMessage = messages(s"yourFlatRateForVehicleExpenses.legend.$userType", flatRateCalc),
        changeMessage = s"travelForWorkYourVehicle.change.hidden.$userType",
        rightTextAlign = true
      ))
  }
}
