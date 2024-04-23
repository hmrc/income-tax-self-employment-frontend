package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object TradingAllowance extends OneQuestionPage[BigDecimal] {
  override def toString: String = "tradingAllowance"
}
