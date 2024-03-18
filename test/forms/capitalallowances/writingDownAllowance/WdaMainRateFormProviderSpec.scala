package forms.capitalallowances.writingDownAllowance

import base.forms.StandardBooleanFormProviderSpec

class WdaMainRateFormProviderSpec
    extends StandardBooleanFormProviderSpec(
      errorPrefix = "wdaMainRate",
      mkForm = new WdaMainRateFormProvider().apply
    )
