package forms.capitalallowances.writingDownAllowance

import base.forms.StandardCurrencyFormProvider

class WdaSpecialRateClaimAmountFormProviderSpec
    extends StandardCurrencyFormProvider(
      "wdaSpecialRateClaimAmount",
      new WdaSpecialRateClaimAmountFormProvider().apply
    )
