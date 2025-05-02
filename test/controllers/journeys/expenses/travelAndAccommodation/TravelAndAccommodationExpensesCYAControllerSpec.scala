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

package controllers.journeys.expenses.travelAndAccommodation

import base.SpecBase
import base.cyaPages.CYAOnPageLoadControllerBaseSpec
import common.TestApp.buildAppFromUserType
import controllers.journeys.clearDependentPages
import models.NormalMode
import models.common.Journey.ExpensesTravelForWork
import models.common.UserType.Individual
import models.common.{BusinessId, Journey, TaxYear, UserType}
import models.database.UserAnswers
import models.journeys.expenses.travelAndAccommodation.YourFlatRateForVehicleExpenses.{Actualcost, Flatrate}
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseType, VehicleType, YourFlatRateForVehicleExpenses}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages.CostsNotCoveredPage
import pages.expenses.travelAndAccommodation._
import play.api.i18n.Messages
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContentAsEmpty, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, redirectLocation, route, status, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.expenses.travelAndAccommodation._

import scala.concurrent.Future

class TravelAndAccommodationExpensesCYAControllerSpec extends CYAOnPageLoadControllerBaseSpec {

  override val pageHeading: String = TravelAndAccommodationCYAPage.toString

  def onPageLoadCall: (TaxYear, BusinessId) => Call = routes.TravelAndAccommodationExpensesCYAController.onPageLoad
  def onSubmitCall: (TaxYear, BusinessId) => Call   = routes.TravelAndAccommodationExpensesCYAController.onSubmit

  lazy val onwardRoute: String = routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, NormalMode).url

  protected implicit lazy val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, onSubmitCall(taxYear, businessId).url)
  protected val journey: Journey                                               = ExpensesTravelForWork

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
    .set(VehicleExpensesPage, vehicleExpenses, Some(businessId))
    .success
    .value

  val simplifiedAnswers: UserAnswers =
    userAnswers
      .set(SimplifiedExpensesPage, true, Some(businessId))
      .success
      .value

  val submissionData: JsObject = userAnswers.data

  override val testDataCases: List[JsObject] = List(submissionData)

  override def expectedSummaryList(
      userAnswers: UserAnswers,
      taxYear: TaxYear,
      businessId: BusinessId,
      userType: UserType
  )(implicit messages: Messages): SummaryList = {
    val rows = List(
      VehicleExpenseTypeSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleNameSummary.row(userAnswers, taxYear, businessId, userType, index),
      VehicleTypeSummary.row(userAnswers, taxYear, businessId, index),
      SimplifiedExpensesSummary.row(userAnswers, taxYear, businessId, userType, index),
      VehicleFlatRateChoiceSummary.row(taxYear, businessId, userAnswers, userType, index),
      TravelForWorkYourMileageSummary.row(taxYear, businessId, userAnswers, userType, index),
      YourFlatRateForVehicleExpensesSummary.row(taxYear, businessId, userAnswers, userType),
      CostsNotCoveredSummary.row(userAnswers, taxYear, businessId, userType),
      VehicleExpensesSummary.row(taxYear, businessId, userAnswers, userType, index)
    ).flatten

    SummaryList(rows = rows, classes = "govuk-!-margin-bottom-7")
  }

  "redirect to AddAnotherVehiclePage on submit" in new TestScenario(Individual, Some(userAnswers)) {
    val result: Future[Result] = route(application, postRequest).value

    status(result) shouldBe 303
    redirectLocation(result).value shouldBe onwardRoute
  }

  "not display VehicleFlatRateChoice row when simplified expenses are enabled" in new TestScenario(Individual, Some(simplifiedAnswers)) {
    implicit val messages: Messages = SpecBase.messages(application)
    val summaryList                 = expectedSummaryList(simplifiedAnswers, taxYear, businessId, Individual)

    summaryList.rows.exists(
      _.key.content.asHtml.toString.contains("Do you want to calculate a flat rate?")
    ) shouldBe false
  }

  "display actual costs answer row when actual costs is selected" in {
    val modifiedAnswers = userAnswers
      .set(VehicleFlatRateChoicePage, false, Some(businessId))
      .success
      .value
      .set(YourFlatRateForVehicleExpensesPage, Actualcost, Some(businessId))
      .success
      .value
      .set(VehicleExpensesPage, vehicleExpenses, Some(businessId))
      .success
      .value

    val application                 = buildAppFromUserType(Individual, Some(modifiedAnswers))
    implicit val messages: Messages = SpecBase.messages(application)
    val summaryList                 = expectedSummaryList(modifiedAnswers, taxYear, businessId, Individual)

    summaryList.rows
      .find(_.key.content.asHtml.toString.contains("flat rate of £"))
      .value
      .value
      .content
      .asHtml
      .toString
      .trim shouldBe "Actual costs"
  }

  // TODO These tests will be addressed in the it tests using IndustrySectorsDb model
  /*  "clear YourFlatRateForVehicleExpensesPage, VehicleFlatRateChoicePage and VehicleExpensesPage when changing simplified expenses answer to Yes" in {
    val initialAnswers = userAnswers.set(SimplifiedExpensesPage, false, Some(businessId)).success.value
    val updatedAnswers = clearDependentPages(SimplifiedExpensesPage, true, initialAnswers, businessId).futureValue

    updatedAnswers.get(YourFlatRateForVehicleExpensesPage, businessId) shouldBe None
    updatedAnswers.get(VehicleFlatRateChoicePage, businessId) shouldBe None
    updatedAnswers.get(VehicleExpensesPage, businessId) shouldBe None
  }

  "clear TravelForWorkYourMileagePage and CostsNotCoveredPage when changing VehicleFlatRateChoicePage expenses answer to No" in {
    val initialAnswers = userAnswers.set(VehicleFlatRateChoicePage, true, Some(businessId)).success.value
    val updatedAnswers = clearDependentPages(VehicleFlatRateChoicePage, false, initialAnswers, businessId).futureValue

    updatedAnswers.get(TravelForWorkYourMileagePage, businessId) shouldBe None
    updatedAnswers.get(CostsNotCoveredPage, businessId) shouldBe None
  }*/

  "clear CostsNotCoveredPage when changing YourFlatRateForVehicleExpensesPage answer to Actual costs" in {
    val initialAnswers = userAnswers.set(YourFlatRateForVehicleExpensesPage, Flatrate, Some(businessId)).success.value
    val updatedAnswers = clearDependentPages(YourFlatRateForVehicleExpensesPage, Actualcost, initialAnswers, businessId).futureValue

    updatedAnswers.get(CostsNotCoveredPage, businessId) shouldBe None
  }

  "clear VehicleExpensesPage when changing YourFlatRateForVehicleExpensesPage answer to Flat rate" in {
    val initialAnswers = userAnswers.set(YourFlatRateForVehicleExpensesPage, Actualcost, Some(businessId)).success.value
    val updatedAnswers = clearDependentPages(YourFlatRateForVehicleExpensesPage, Flatrate, initialAnswers, businessId).futureValue

    updatedAnswers.get(VehicleExpensesPage, businessId) shouldBe None
  }

}
