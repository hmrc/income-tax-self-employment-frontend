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

package pages.expenses

import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import models.journeys.expenses.travelAndAccommodation.{VehicleDetailsDb, YourFlatRateForVehicleExpenses}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pages.expenses.travelAndAccommodation.SimplifiedExpensesPage
import play.api.libs.json.JsPath

class SimplifiedExpensesPageSpec extends PlaySpec with MockitoSugar {

  val testVehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("Car"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(YourFlatRateForVehicleExpenses.Flatrate),
    costsOutsideFlatRate = Some(BigDecimal("100.00")),
    vehicleExpenses = Some(BigDecimal("300.00"))
  )

  "SimplifiedExpensesPage" should {

    "return the correct string" in {
      SimplifiedExpensesPage.toString mustBe "simplifiedExpenses"
    }

    "return the correct path" in {
      val expectedPath = JsPath \ "simplifiedExpenses"
      SimplifiedExpensesPage.path(None) mustBe expectedPath
    }

    "clearDependentPageDataAndUpdate" when {

      "when the value selected is 'true'" in {
        SimplifiedExpensesPage.clearDependentPageDataAndUpdate(value = true, testVehicleDetails) mustBe testVehicleDetails
      }

      "when the value selected is 'false'" in {
        SimplifiedExpensesPage.clearDependentPageDataAndUpdate(value = false, testVehicleDetails) mustBe VehicleDetailsDb(Some("Car"), Some(CarOrGoodsVehicle), Some(false), None, Some(100000), None, Some(100.00), None)
      }

    }

  }
}
