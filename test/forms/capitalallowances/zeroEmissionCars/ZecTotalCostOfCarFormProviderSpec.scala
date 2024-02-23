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

import forms.behaviours.{BigDecimalFieldBehaviours, IntFieldBehaviours}
import models.common.UserType.Individual
import models.common.{MoneyBounds, UserType}
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class ZecTotalCostOfCarFormProviderSpec extends BigDecimalFieldBehaviours with IntFieldBehaviours with MoneyBounds {

  val minimumVal: BigDecimal                = minimumOneValue
  val maximumVal: BigDecimal                = maxAmountValue
  val requiredError: String              = "zecTotalCostOfCar.error.required"
  val nonNumericError: String            = "error.nonNumeric"
  val noDecimalsError: String            = "error.nonDecimal"
  val lessThanMinError: String          = "error.minimumOne"
  val overMaxError: String               = "expenses.error.overMax"

  val userType: UserType = Individual
  val fieldName          = "value"

  val validDataGenerator: Gen[String]    = intsInRangeWithCommas(minimumVal.toInt, maximumVal.toInt)
  val dataDecimalsGenerator: Gen[String] = bigDecimalsInRangeWithCommas(minimumVal, maximumVal)

  "ZecTotalCostOfCarFormProvider should" - {
    val form: Form[BigDecimal] = new ZecTotalCostOfCarFormProvider()(userType)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      FormError(fieldName, nonNumericError),
      FormError(fieldName, noDecimalsError)
    )

    behave like intFieldWithMinimum(
      form,
      fieldName,
      minimumVal.toInt,
      FormError(fieldName, lessThanMinError)
    )

    behave like intFieldWithMaximum(
      form,
      fieldName,
      maximumVal.toInt,
      FormError(fieldName, overMaxError)
    )

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, s"$requiredError.$userType")
    )
  }
}
