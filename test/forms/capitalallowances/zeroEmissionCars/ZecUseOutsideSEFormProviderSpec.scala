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

package forms.capitalallowances.zeroEmissionCars

import forms.behaviours.{IntFieldBehaviours, OptionFieldBehaviours}
import models.common.UserType.Individual
import models.journeys.capitalallowances.zeroEmissionCars.ZecUseOutsideSE
import play.api.data.FormError
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

class ZecUseOutsideSEFormProviderSpec extends OptionFieldBehaviours with IntFieldBehaviours {

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())
  private val maxPercentage           = 100
  private val minPercentage           = 0
  private val form                    = ZecUseOutsideSEFormProvider(Individual)
  private val radioFieldName          = "radioPercentage"
  private val amountFieldName         = "optDifferentAmount"
  private val requiredError           = s"zecUseOutsideSE.error.required.$Individual"
  private val amountRequiredError     = "error.required"
  private val nonNumericError         = "error.nonNumeric"
  private val noDecimalsError         = "error.nonDecimal"
  private val lessThanZeroError       = "error.lessThanZero"
  private val overMaxError            = "zecUseOutsideSE.error.overMax"
  private val validRadioValues        = ZecUseOutsideSE.values

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

  s"for Int input field: $amountFieldName, form should" - {

    behave like fieldThatBindsValidData(
      form,
      amountFieldName,
      intsInRangeWithCommas(minPercentage, maxPercentage)
    )

    behave like intField(
      form,
      amountFieldName,
      FormError(amountFieldName, nonNumericError),
      FormError(amountFieldName, noDecimalsError)
    )

    behave like mandatoryField(
      form,
      amountFieldName,
      FormError(amountFieldName, amountRequiredError)
    )

    behave like intFieldWithMaximum(
      form,
      amountFieldName,
      maxPercentage,
      FormError(amountFieldName, overMaxError, Seq(maxPercentage))
    )

    behave like intFieldWithMinimum(
      form,
      amountFieldName,
      minPercentage,
      FormError(amountFieldName, lessThanZeroError, Seq(minPercentage))
    )
  }

}
