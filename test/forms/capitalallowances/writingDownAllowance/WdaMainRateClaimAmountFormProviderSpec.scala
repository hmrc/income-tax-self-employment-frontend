package forms.capitalallowances.writingDownAllowance

import base.forms.StandardCurrencyFormProvider

class WdaMainRateClaimAmountFormProviderSpec
    extends StandardCurrencyFormProvider(
      "wdaMainRateClaimAmount",
      new WdaMainRateClaimAmountFormProvider().apply
    )
