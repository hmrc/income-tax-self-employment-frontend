/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.expenses.travelAndAccommodation

import forms.TravelForWorkYourMileageFormProvider
import forms.behaviours.BigDecimalFieldBehaviours
import models.common.MoneyBounds.maximumValue
import models.common.UserType
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class TravelForWorkYourMileageFormProviderSpec extends BigDecimalFieldBehaviours {

  val vehicle             = "Grey Astra"
  val minimum: BigDecimal = zeroValue
  val maximum: BigDecimal = maximumValue

  val validDataGenerator: Gen[String] = bigDecimalsInRangeWithCommas(minimum, maximum)

  val requiredError     = "travelForWorkYourMileage.error.required"
  val nonNumericError   = "travelForWorkYourMileage.error.nonNumeric"
  val lessThanZeroError = "travelForWorkYourMileage.error.lessThanZero"
  val overMaxError      = "travelForWorkYourMileage.error.overMax"

  def getFormProvider(userType: UserType): Form[BigDecimal] =
    new TravelForWorkYourMileageFormProvider()(userType, vehicle)

  val userTypes: List[UserType] = List(UserType.Individual, UserType.Agent)

  userTypes.foreach { userType =>
    s"form for $userType should" - {

      val form: Form[BigDecimal] = getFormProvider(userType)

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        validDataGenerator
      )

      behave like bigDecimalField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, s"$nonNumericError.$userType", Seq(vehicle))
      )

      behave like bigDecimalFieldWithMinimum(
        form,
        fieldName,
        minimum,
        expectedError = FormError(fieldName, s"$lessThanZeroError.$userType", Seq(vehicle))
      )

      behave like bigDecimalFieldWithMaximum(
        form,
        fieldName,
        maximum,
        expectedError = FormError(fieldName, s"$overMaxError.$userType", Seq(vehicle))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, s"$requiredError.$userType", Seq(vehicle))
      )
    }
  }
}
