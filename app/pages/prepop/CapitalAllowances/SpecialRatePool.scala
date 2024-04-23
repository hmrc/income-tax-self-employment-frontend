package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object SpecialRatePool extends OneQuestionPage[BigDecimal] {
  override def toString: String = "specialRatePool"
}
