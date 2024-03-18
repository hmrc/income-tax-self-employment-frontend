package forms.capitalallowances.writingDownAllowance

import base.forms.StandardCurrencyFormProvider

class WdaSingleAssetClaimAmountsFormProviderSpec
    extends StandardCurrencyFormProvider(
      "wdaSingleAssetClaimAmounts",
      new WdaSingleAssetClaimAmountsFormProvider().apply
    )
