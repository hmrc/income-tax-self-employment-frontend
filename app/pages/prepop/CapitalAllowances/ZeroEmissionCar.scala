package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object ZeroEmissionCar extends OneQuestionPage[BigDecimal] {
  override def toString: String = "zeroEmissionCar"
}
