package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object MainRatePool extends OneQuestionPage[BigDecimal] {
  override def toString: String = "mainRatePool"
}
