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
      VehicleFlatRateChoicePage.clearDependentPageDataAndUpdate(flatRateChoice = false, testVehicleDetails) mustBe VehicleDetailsDb(Some("Car"), Some(CarOrGoodsVehicle), Some(true), Some(true), None, None, None, Some(300.00))
    }

    "do not clearPageData when the flag is true" in {
      VehicleFlatRateChoicePage.clearDependentPageDataAndUpdate(flatRateChoice = true, testVehicleDetails) mustBe testVehicleDetails
    }

  }
}
