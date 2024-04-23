package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object ZeroEmissionGoodsVehicle extends OneQuestionPage[BigDecimal] {
  override def toString: String = "zeroEmissionGoodsVehicle"
}
