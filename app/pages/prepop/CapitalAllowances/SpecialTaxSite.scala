package pages.prepop.CapitalAllowances

import pages.OneQuestionPage

case object SpecialTaxSite extends OneQuestionPage[BigDecimal] {
  override def toString: String = "specialTaxSite"
}
