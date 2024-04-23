package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object ElectricVehicleChargePoint extends OneQuestionPage[BigDecimal] {
  override def toString: String = "electricVehicleChargePoint"
}
