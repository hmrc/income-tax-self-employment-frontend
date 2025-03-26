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
import models.journeys.expenses.travelAndAccommodation.TravelAndAccommodationExpenseType.{LeasedVehicles, MyOwnVehicle}
import models.journeys.expenses.travelAndAccommodation.{TravelAndAccommodationExpenseType, VehicleType}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pages._
import pages.expenses.travelAndAccommodation._

class TravelAndAccommodationNavigatorSpec extends SpecBase {

  val navigator = new TravelAndAccommodationNavigator

  case object UnknownPage extends Page

  "TravelAndAccommodationNavigator" - {
    "navigating to the next page" - {
      "in NormalMode" - {
        val mode = NormalMode

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }

        "navigate to TravelForWorkYourVehiclePage from TravelAndAccommodationExpenseTypePage" in {
          val expectedResult = routes.TravelForWorkYourVehicleController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, Set[TravelAndAccommodationExpenseType](MyOwnVehicle, LeasedVehicles), Some(businessId))
            .toOption
            .value

          navigator.nextPage(TravelAndAccommodationExpenseTypePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to VehicleTypePage from TravelForWorkYourVehiclePage" in {
          val expectedResult = routes.VehicleTypeController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, Set[TravelAndAccommodationExpenseType](MyOwnVehicle, LeasedVehicles), Some(businessId))
            .toOption
            .value
            .set(TravelForWorkYourVehiclePage, "NewCar")
            .toOption
            .value

          navigator.nextPage(TravelForWorkYourVehiclePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to SimplifiedExpensesPage from VehicleTypePage" in {
          val expectedResult = routes.SimplifiedExpensesController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(VehicleTypePage, VehicleType.CarOrGoodsVehicle, Some(businessId))
            .toOption
            .value

          navigator.nextPage(VehicleTypePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to UseSimplifiedExpensesPage from SimplifiedExpensesPage when option selected is 'true'" in {
          val expectedResult = routes.UseSimplifiedExpensesController.onPageLoad(taxYear, businessId)
          val ua = emptyUserAnswers
            .set(SimplifiedExpensesPage, true, Some(businessId))
            .toOption
            .value

          navigator.nextPage(SimplifiedExpensesPage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to VehicleFlatRateChoicePage from SimplifiedExpensesPage when option selected is 'false'" in {
          val expectedResult = routes.VehicleFlatRateChoiceController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(SimplifiedExpensesPage, false, Some(businessId))
            .toOption
            .value

          navigator.nextPage(SimplifiedExpensesPage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to VehicleExpensesPage from VehicleFlatRateChoicePage when option selected is 'false'" in {
          val expectedResult = routes.VehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(TravelAndAccommodationExpenseTypePage, Set[TravelAndAccommodationExpenseType](MyOwnVehicle, LeasedVehicles), Some(businessId))
            .toOption
            .value
            .set(VehicleFlatRateChoicePage, false, Some(businessId))
            .toOption
            .value

          navigator.nextPage(VehicleFlatRateChoicePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }

        "navigate to YourFlatRateForVehicleExpensesPage from TravelForWorkYourMileagePage when option selected is 'false'" in {
          val expectedResult = routes.YourFlatRateForVehicleExpensesController.onPageLoad(taxYear, businessId, NormalMode)
          val ua = emptyUserAnswers
            .set(TravelForWorkYourMileagePage, BigDecimal(200), Some(businessId))
            .toOption
            .value

          navigator.nextPage(TravelForWorkYourMileagePage, mode, ua, taxYear, businessId) shouldBe expectedResult
        }
      }

      "in CheckMode" - {
        val mode = CheckMode

        "page does not exist" - {
          "navigate to the JourneyRecoveryController" in {
            val expectedResult = standard.routes.JourneyRecoveryController.onPageLoad()

            navigator.nextPage(UnknownPage, mode, emptyUserAnswers, taxYear, businessId) shouldBe expectedResult
          }
        }
      }
    }
  }

}
