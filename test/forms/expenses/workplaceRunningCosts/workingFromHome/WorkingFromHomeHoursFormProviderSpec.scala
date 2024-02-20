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

package forms.expenses.workplaceRunningCosts.workingFromHome

import forms.behaviours.IntFieldBehaviours
import models.common.UserType.Individual
import play.api.data.FormError
import play.api.i18n.{DefaultMessagesApi, Lang, MessagesImpl}

class WorkingFromHomeHoursFormProviderSpec extends IntFieldBehaviours {

  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), new DefaultMessagesApi())
  private val maxMonths               = 12

  private val fieldNames: Seq[String] = Seq("value25To50", "value51To100", "value101Plus")
  private val form                    = WorkingFromHomeHoursFormProvider(Individual, maxMonths)
  private val nonNumericError         = "common.error.nonNumeric"
  private val noDecimalsError         = "expenses.error.noDecimals"
  private val lessThanZeroError       = "expenses.error.lessThanZero"
  private val totalOverMaxError       = "workingFromHomeHours.error.overMax.total.individual"

  fieldNames.foreach { fieldName =>
    s"for input field: $fieldName, form should" - {
      val requiredError = s"workingFromHomeHours.error.required.$fieldName"
      val overMaxError  = s"workingFromHomeHours.error.overMax.$fieldName.individual"

      behave like intField(
        form,
        fieldName,
        FormError(fieldName, nonNumericError),
        FormError(fieldName, noDecimalsError)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredError)
      )

      behave like intFieldWithMinimum(
        form,
        fieldName,
        0,
        FormError(fieldName, lessThanZeroError, Seq(0))
      )

      behave like intFieldWithMaximum(
        form,
        fieldName,
        maxMonths,
        FormError(fieldName, overMaxError, Seq(maxMonths.toString))
      )
    }
  }

  "form should return an error when the total number of months entered is greater than the max number of months" in {
    val result = form.bind(Map(fieldNames(0) -> 5.toString, fieldNames(1) -> 2.toString, fieldNames(2) -> 11.toString))

    result.errors must contain only FormError("", totalOverMaxError)
  }
}
