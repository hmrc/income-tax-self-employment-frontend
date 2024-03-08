/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.capitalallowances.electricVehicleChargePoints

import forms.behaviours.{BigDecimalFieldBehaviours, OptionFieldBehaviours}
import forms.capitalallowances.electricVehicleChargePoints.EvcpHowMuchDoYouWantToClaimFormProvider.noDecimalRegexp
import models.common.UserType.Individual
import models.journeys.capitalallowances.electricVehicleChargePoints.EvcpHowMuchDoYouWantToClaim
import play.api.data.FormError
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

class EvcpHowMuchDoYouWantToClaimFormProviderSpec extends OptionFieldBehaviours with BigDecimalFieldBehaviours {

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())
  private val fullAmount              = 12000

  private val radioFieldName      = "howMuchDoYouWantToClaim"
  private val amountFieldName     = "totalCost"
  private val requiredError       = s"evcpHowMuchDoYouWantToClaim.error.required.$Individual"
  private val amountRequiredError = "evcpHowMuchDoYouWantToClaim.error.required.amount"
  private val nonNumericError     = "error.nonNumeric"
  private val noDecimalsError     = "error.nonDecimal"
  private val overMaxError        = "evcpHowMuchDoYouWantToClaim.error.overMax"
  private val validRadioValues    = EvcpHowMuchDoYouWantToClaim.values

  s"for RadioButton input field: $radioFieldName, form should" - {

    behave like optionsField(
      form,
      radioFieldName,
      validRadioValues,
      invalidError = FormError(radioFieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      radioFieldName,
      requiredError = FormError(radioFieldName, requiredError)
    )
  }

  s"for BigDecimal input field: $amountFieldName, form should" - {

    behave like fieldThatBindsValidData(
      form,
      amountFieldName,
      bigDecimalsInRangeWithNoDecimals(zeroValue, fullAmount)
    )

    behave like bigDecimalField(
      form,
      amountFieldName,
      FormError(amountFieldName, nonNumericError)
    )

    behave like bigDecimalFieldWithMaximum(
      form,
      amountFieldName,
      fullAmount,
      FormError(amountFieldName, overMaxError, Seq(fullAmount))
    )

    behave like mandatoryField(
      form,
      amountFieldName,
      FormError(amountFieldName, amountRequiredError)
    )

    behave like bigDecimalFieldWithRegex(
      form,
      amountFieldName,
      noDecimalRegexp,
      BigDecimal(400),
      BigDecimal(400.20),
      FormError(amountFieldName, noDecimalsError, Seq(noDecimalRegexp))
    )
  }

}
