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

package forms

import forms.behaviours.OptionFieldBehaviours
import forms.income.HowMuchTradingAllowanceFormProvider
import models.HowMuchTradingAllowance
import play.api.data.FormError

class HowMuchTradingAllowanceFormProviderSpec extends OptionFieldBehaviours {

  ".value" - {

    val fieldName      = "value"
    val isAgentString  = "individual"
    val turnoverAmount = "1000.00"
    val requiredKey    = s"howMuchTradingAllowance.error.required.$isAgentString"

    val form = new HowMuchTradingAllowanceFormProvider()(isAgentString, turnoverAmount)

    behave like optionsField[HowMuchTradingAllowance](
      form,
      fieldName,
      validValues = HowMuchTradingAllowance.values,
      invalidError = FormError(fieldName, "error.invalid", Seq(turnoverAmount))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(turnoverAmount))
    )

  }

}
