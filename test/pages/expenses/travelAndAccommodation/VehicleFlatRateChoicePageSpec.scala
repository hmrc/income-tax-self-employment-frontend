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

package pages.expenses.travelAndAccommodation

import models.journeys.expenses.travelAndAccommodation.{VehicleDetailsDb, YourFlatRateForVehicleExpenses}
import models.journeys.expenses.travelAndAccommodation.VehicleType.CarOrGoodsVehicle
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsPath

class VehicleFlatRateChoicePageSpec extends PlaySpec with MockitoSugar {

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

  "VehicleFlatRateChoicePage" should {

    "return the correct string" in {
      VehicleFlatRateChoicePage.toString mustBe "vehicleFlatRateChoice"
    }

    "return the correct path" in {
      val expectedPath = JsPath \ "vehicleFlatRateChoice"
      VehicleFlatRateChoicePage.path(None) mustBe expectedPath
    }

    "clearPageData when the flag is false" in {
      VehicleFlatRateChoicePage.clearDependentPageDataAndUpdate(flatRateChoice = false, testVehicleDetails) mustBe VehicleDetailsDb(
        Some("Car"),
        Some(CarOrGoodsVehicle),
        Some(true),
        Some(false),
        None,
        None,
        None,
        Some(300.00))
    }

    "do not clearPageData when the flag is true" in {
      VehicleFlatRateChoicePage.clearDependentPageDataAndUpdate(flatRateChoice = true, testVehicleDetails) mustBe testVehicleDetails
    }

  }
}
