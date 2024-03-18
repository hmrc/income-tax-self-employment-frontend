package forms.capitalallowances.writingDownAllowance

import base.forms.BooleanFormUserTypeAwareFormProvider

class WdaSpecialRateFormProviderSpec
    extends BooleanFormUserTypeAwareFormProvider(
      errorPrefix = "wdaSpecialRate",
      mkForm = new WdaSpecialRateFormProvider().apply
    )
