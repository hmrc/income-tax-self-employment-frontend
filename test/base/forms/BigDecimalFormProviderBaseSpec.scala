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

package base.forms

import forms.behaviours.BigDecimalFieldBehaviours
import models.common.UserType
import models.common.UserType.{Agent, Individual}
import play.api.data.{Form, FormError}

abstract case class BigDecimalFormProviderBaseSpec(formProviderName: String) extends BigDecimalFieldBehaviours {

  private val fieldName = "value"

  protected val minimum: BigDecimal = 0
  protected val maximum: BigDecimal = 100000000000.00

  private val userTypes: List[UserType] = List(Individual, Agent)
  private val validDataGenerator        = bigDecimalsInRangeWithCommas(minimum, maximum)

  val requiredError: String
  val nonNumericError: String
  val lessThanZeroError: String
  val overMaxError: String

  def getFormProvider(userType: UserType): Form[BigDecimal]

  userTypes.foreach { userType =>
    s"$formProviderName for $userType, form should" - {
      val form: Form[BigDecimal] = getFormProvider(userType)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        validDataGenerator
      )

      behave like bigDecimalField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, s"$nonNumericError.$userType")
      )

      behave like bigDecimalFieldWithMinimum(
        form,
        fieldName,
        minimum,
        expectedError = FormError(fieldName, s"$lessThanZeroError.$userType", Seq(minimum))
      )

      behave like bigDecimalFieldWithMaximum(
        form,
        fieldName,
        maximum,
        expectedError = FormError(fieldName, s"$overMaxError.$userType", Seq(maximum))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredError.$userType")
      )
    }
  }
}
