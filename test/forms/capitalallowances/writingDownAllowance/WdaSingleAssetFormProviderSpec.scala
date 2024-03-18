package forms.capitalallowances.writingDownAllowance

import base.forms.StandardBooleanFormProviderSpec

class WdaSingleAssetFormProviderSpec
    extends StandardBooleanFormProviderSpec(
      errorPrefix = "wdaSingleAsset",
      mkForm = new WdaSingleAssetFormProvider().apply
    )
