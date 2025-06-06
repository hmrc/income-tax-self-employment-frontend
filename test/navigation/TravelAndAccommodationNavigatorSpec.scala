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

package navigation

import base.SpecBase
import controllers.journeys.expenses.travelAndAccommodation.routes
import controllers.standard
import models._
import models.journeys.expenses.individualCategories.TravelForWork
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.{LeasedVehicles, MyOwnVehicle}
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.TravelAndAccommodationDisallowableExpensesPage
import pages.expenses.tailoring.individualCategories.TravelForWorkPage
import pages.expenses.travelAndAccommodation._
import pages.travelAndAccommodation.TravelAndAccommodationTotalExpensesPage

class TravelAndAccommodationNavigatorSpec extends SpecBase {

  val navigator = new TravelAndAccommodationNavigator

  case object UnknownPage extends Page

  "TravelAndAccommodationNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        val vehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
          description = Some("NewCar"),
          vehicleType = Some(CarOrGoodsVehicle),
          usedSimplifiedExpenses = Some(true),
          calculateFlatRate = Some(true),
          workMileage = Some(100000),
          expenseMethod = Some(YourFlatRateForVehicleExpenses.Flatrate),
          costsOutsideFlatRate = Some(BigDecimal("100.00"))
        )

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "navigate to TravelForWorkYourVehiclePage from TravelAndAccommodationExpenseTypePage" in {
          val expectedResult = routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, index, NormalMode)
          val ua = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, Set[TravelAndAccommodationExpenseType](MyOwnVehicle, LeasedVehicles), Some(businessId))
            .toOption
            .value

          navigator.nextPage(TravelAndAccommodationExpenseTypePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to VehicleTypePage from TravelForWorkYourVehiclePage" in {
          val expectedResult = routes.VehicleTypeController.onPageLoad(taxYear, businessId, index, NormalMode)

          navigator.nextIndexPage(TravelForWorkYourVehiclePage, mode, vehicleDetails, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to SimplifiedExpensesPage from VehicleTypePage" in {
          val expectedResult = routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, index, NormalMode)
          val vd             = vehicleDetails.copy(vehicleType = Some(VehicleType.CarOrGoodsVehicle))

          navigator.nextIndexPage(VehicleTypePage, mode, vd, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to UseSimplifiedExpensesPage from SimplifiedExpensesPage when option selected is 'true'" in {
          val expectedResult = routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId, index)
          val vd             = vehicleDetails.copy(usedSimplifiedExpenses = Some(true))

          navigator.nextIndexPage(SimplifiedExpensesPage, mode, vd, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to VehicleFlatRateChoicePage from SimplifiedExpensesPage when option selected is 'false'" in {
          val expectedResult = routes.VehicleFlatRateChoiceController.onPageLoad(taxYear, businessId, index, NormalMode)
          val vd             = vehicleDetails.copy(usedSimplifiedExpenses = Some(false))

          navigator.nextIndexPage(SimplifiedExpensesPage, mode, vd, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to VehicleExpensesPage from VehicleFlatRateChoicePage when option selected is 'false'" in {
          val expectedResult = routes.VehicleExpensesController.onPageLoad(taxYear, businessId, index, NormalMode)
          val vd             = vehicleDetails.copy(calculateFlatRate = Some(false))

          navigator.nextIndexPage(VehicleFlatRateChoicePage, mode, vd, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to YourFlatRateForVehicleExpensesPage from TravelForWorkYourMileagePage when option selected is 'false'" in {
          val expectedResult = routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode)
          val vd             = vehicleDetails.copy(workMileage = Some(BigDecimal(200)))

          navigator.nextIndexPage(TravelForWorkYourMileagePage, mode, vd, taxYear, businessId, index) shouldBe expectedResult
        }

        "navigate to DisallowableTransportAndAccommodationPage from PublicTransportAndAccommodationExpensesPage" +
          " when TravelForWorkPage option selected is 'YesDisallowable'" in {
            val expectedResult = routes.DisallowableTransportAndAccommodationController.onPageLoad(taxYear, businessId, NormalMode)
            val ua = emptyUserAnswers
              .set(TravelForWorkPage, TravelForWork.YesDisallowable, Some(businessId))
              .toOption
              .value
              .set(PublicTransportAndAccommodationExpensesPage, BigDecimal(200), Some(businessId))
              .toOption
              .value

            navigator.nextPage(PublicTransportAndAccommodationExpensesPage, mode, ua, taxYear, businessId) shouldBe expectedResult
          }

        "navigate to TravelAndAccommodationCYA from PublicTransportAndAccommodationExpensesPage" +
          " when TravelForWorkPage option selected is 'YesAllowable'" ignore {
            val expectedResult = routes.DisallowableTransportAndAccommodationController.onPageLoad(taxYear, businessId, NormalMode)
            val ua = emptyUserAnswers
              .set(TravelForWorkPage, TravelForWork.YesAllowable, Some(businessId))
              .toOption
              .value
              .set(PublicTransportAndAccommodationExpensesPage, BigDecimal(200), Some(businessId))
              .toOption
              .value

            navigator.nextPage(PublicTransportAndAccommodationExpensesPage, mode, ua, taxYear, businessId) shouldBe expectedResult
          }

      }

      "in CheckMode" - {
        val mode = CheckMode

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }

          "navigate to AddAnotherVehicle change link from RemoveVehicle" in {
            val expectedResult = routes.AddAnotherVehicleController.onPageLoad(taxYear, businessId, CheckMode)
            val ua = emptyUserAnswers
              .set(TravelForWorkYourVehiclePage, "vehicle", Some(businessId))
              .toOption
              .value
              .set(RemoveVehiclePage, true, Some(businessId))
              .toOption
              .value

            navigator.nextPage(RemoveVehiclePage, mode, ua, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }

    "navigating to the next  short journey page" - {
      "in NormalMode " - {
        val mode = NormalMode

        val travelExpensesDb: TravelExpensesDb = TravelExpensesDb(
          totalTravelExpenses = Some(200),
          disallowableTravelExpenses = Some(450)
        )

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()
            val ua = emptyUserAnswers
              .set(TravelForWorkYourVehiclePage, "vehicle", Some(businessId))
              .toOption
              .value
              .set(RemoveVehiclePage, true, Some(businessId))
              .toOption
              .value

            navigator.nextTravelExpensesPage(UnknownPage, mode, travelExpensesDb, taxYear, businessId, ua) shouldBe expectedResult
          }
        }

        "navigate to TravelAndAccommodationDisallowableExpensesPage from TravelAndAccommodationTotalExpensesPage" +
          " when TravelForWorkPage option selected is 'YesDisallowable'" in {
            val expectedResult = routes.TravelAndAccommodationDisallowableExpensesController.onPageLoad(taxYear, businessId, NormalMode)
            val ua = emptyUserAnswers
              .set(TravelForWorkPage, TravelForWork.YesDisallowable, Some(businessId))
              .toOption
              .value
              .set(TravelAndAccommodationTotalExpensesPage, BigDecimal(200), Some(businessId))
              .toOption
              .value

            navigator.nextTravelExpensesPage(
              TravelAndAccommodationTotalExpensesPage,
              mode,
              travelExpensesDb,
              taxYear,
              businessId,
              ua) shouldBe expectedResult
          }

        "navigate to TravelAndAccommodationCYA from TravelAndAccommodationTotalExpensesPage" +
          "when TravelForWorkPage option selected is 'YesAllowable'" ignore { // TODO direct to new CYA page
            val expectedResult = controllers.journeys.routes.TaskListController.onPageLoad(taxYear)
            val ua = emptyUserAnswers
              .set(TravelForWorkPage, TravelForWork.YesAllowable, Some(businessId))
              .toOption
              .value
              .set(TravelAndAccommodationTotalExpensesPage, BigDecimal(200), Some(businessId))
              .toOption
              .value

            navigator.nextTravelExpensesPage(
              TravelAndAccommodationTotalExpensesPage,
              mode,
              travelExpensesDb,
              taxYear,
              businessId,
              ua) shouldBe expectedResult
          }

        "navigate to TravelAndAccommodationCYAage from TravelAndAccommodationDisallowableExpensesPage" in { // TODO navigate to CYA page
          val expectedResult = controllers.journeys.routes.TaskListController.onPageLoad(taxYear)

          val ua = emptyUserAnswers
            .set(TravelForWorkPage, TravelForWork.YesAllowable, Some(businessId))
            .toOption
            .value
            .set(TravelAndAccommodationTotalExpensesPage, BigDecimal(200), Some(businessId))
            .toOption
            .value

          navigator.nextTravelExpensesPage(
            TravelAndAccommodationDisallowableExpensesPage,
            mode,
            travelExpensesDb,
            taxYear,
            businessId,
            ua) shouldBe expectedResult
        }
      }
    }
  }

}
