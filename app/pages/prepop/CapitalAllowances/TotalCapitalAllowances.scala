package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object TotalCapitalAllowances extends OneQuestionPage[BigDecimal] {
  override def toString: String = "totalCapitalAllowances"
}
