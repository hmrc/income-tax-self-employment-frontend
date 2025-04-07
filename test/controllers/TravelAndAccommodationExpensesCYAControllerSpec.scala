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

package controllers

import base.cyaPages.{CYAOnPageLoadControllerBaseSpec, CYAOnSubmitControllerBaseSpec}
import controllers.journeys.expenses.travelAndAccommodation.routes
import models.common.Journey.ExpensesTravelForWork
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseType, VehicleType, YourFlatRateForVehicleExpenses}
import pages.CostsNotCoveredPage
import pages.expenses.travelAndAccommodation._
import play.api.i18n.Messages
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsObject
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.travelAndAccommodation._

class TravelAndAccommodationExpensesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec with CYAOnSubmitControllerBaseSpec {

  override val pageHeading: String = TravelAndAccommodationCYAPage.toString

  def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.TravelAndAccommodationExpensesCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.TravelAndAccommodationExpensesCYAController.onSubmit

  override protected val journey: Journey = ExpensesTravelForWork

  private val expenseTypes: Set[TravelAndAccommodationExpenseType] =
    Set(TravelAndAccommodationExpenseType.MyOwnVehicle, TravelAndAccommodationExpenseType.LeasedVehicles)
  private val vehicleName                 = "My Car"
  private val vehicleType                 = VehicleType.CarOrGoodsVehicle
  private val simplifiedExpenses          = false
  private val flatRateChoice              = true
  private val mileage: BigDecimal         = 1000
  private val flatRateExpense             = YourFlatRateForVehicleExpenses.Flatrate
  private val costsNotCovered: BigDecimal = 500
  private val vehicleExpenses: BigDecimal = 2000

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(TravelAndAccommodationExpenseTypePage, expenseTypes, Some(businessId))
    .success
    .value
    .set(TravelForWorkYourVehiclePage, vehicleName, Some(businessId))
    .success
    .value
    .set(VehicleTypePage, vehicleType, Some(businessId))
    .success
    .value
    .set(SimplifiedExpensesPage, simplifiedExpenses, Some(businessId))
    .success
    .value
    .set(VehicleFlatRateChoicePage, flatRateChoice, Some(businessId))
    .success
    .value
    .set(TravelForWorkYourMileagePage, mileage, Some(businessId))
    .success
    .value
    .set(YourFlatRateForVehicleExpensesPage, flatRateExpense, Some(businessId))
    .success
    .value
    .set(CostsNotCoveredPage, costsNotCovered, Some(businessId))
    .success
    .value
//    .set(VehicleExpensesPage, vehicleExpenses, Some(businessId))
//    .success
//    .value

  override val submissionData: JsObject = userAnswers.data

  override val testDataCases: List[JsObject] = List(submissionData)

  override def expectedSummaryList(
      userAnswers: UserAnswers,
      taxYear: TaxYear,
      businessId: BusinessId,
      userType: UserType
  )(implicit messages: Messages): SummaryList = {
    val rows = List(
      VehicleExpenseTypeSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleNameSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleTypeSummary.row(userAnswers, taxYear, businessId),
      SimplifiedExpensesSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleFlatRateChoiceSummary.row(taxYear, businessId, userAnswers, userType),
      TravelForWorkYourMileageSummary.row(taxYear, businessId, userAnswers, userType),
      YourFlatRateForVehicleExpensesSummary.row(taxYear, businessId, userAnswers, userType),
      CostsNotCoveredSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleExpensesSummary.row(taxYear, businessId, userAnswers, userType)
    ).flatten

    SummaryList(rows = rows, classes = "govuk-!-margin-bottom-7")
  }

}
