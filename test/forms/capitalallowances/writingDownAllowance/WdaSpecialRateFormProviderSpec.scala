package forms.capitalallowances.writingDownAllowance

import base.forms.StandardBooleanFormProviderSpec

class WdaSpecialRateFormProviderSpec
    extends StandardBooleanFormProviderSpec(
      errorPrefix = "wdaSpecialRate",
      mkForm = new WdaSpecialRateFormProvider().apply
    )
