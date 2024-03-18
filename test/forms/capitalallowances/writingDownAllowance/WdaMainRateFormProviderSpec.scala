package forms.capitalallowances.writingDownAllowance

import base.forms.BooleanFormUserTypeAwareFormProvider

class WdaMainRateFormProviderSpec
    extends BooleanFormUserTypeAwareFormProvider(
      errorPrefix = "wdaMainRate",
      mkForm = new WdaMainRateFormProvider().apply
    )
