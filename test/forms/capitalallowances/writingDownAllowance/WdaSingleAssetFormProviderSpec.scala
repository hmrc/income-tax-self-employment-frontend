package forms.capitalallowances.writingDownAllowance

import base.forms.BooleanFormUserTypeAwareFormProvider

class WdaSingleAssetFormProviderSpec
    extends BooleanFormUserTypeAwareFormProvider(
      errorPrefix = "wdaSingleAsset",
      mkForm = new WdaSingleAssetFormProvider().apply
    )
