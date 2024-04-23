package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object AnnualInvestment extends OneQuestionPage[BigDecimal] {
  override def toString: String = "annualInvestment"
}
