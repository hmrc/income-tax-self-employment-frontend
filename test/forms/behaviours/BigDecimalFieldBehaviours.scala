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

package forms.behaviours

import play.api.data.{Form, FormError}

trait BigDecimalFieldBehaviours extends FieldBehaviours {

  def bigDecimalField(form: Form[_], fieldName: String, nonNumericError: FormError): Unit =
    s"not bind non-numeric numbers, return ${formErrorToString(nonNumericError)}" in {

      forAll(nonNumerics -> "nonNumeric") { nonNumeric =>
        val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
        result.errors must contain only nonNumericError
      }
    }

  def bigDecimalFieldWithMinimum(form: Form[_], fieldName: String, minimum: BigDecimal, expectedError: FormError): Unit =
    s"not bind big decimals below $minimum, return ${formErrorToString(expectedError)}" in {

      forAll(currencyBelowValue(minimum) -> "bigDecimalBelowMin") { number: BigDecimal =>
        val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

  def bigDecimalFieldWithMaximum(form: Form[_], fieldName: String, maximum: BigDecimal, expectedError: FormError): Unit =
    s"not bind big decimals above $maximum, return ${formErrorToString(expectedError)}" in {

      val result = form.bind(Map(fieldName -> (maximum + 1).toString)).apply(fieldName)
      result.errors must contain only expectedError
    }

  def bigDecimalFieldWithRegex(form: Form[_],
                               fieldName: String,
                               regex: String,
                               validValue: BigDecimal,
                               invalidValue: BigDecimal,
                               expectedError: FormError): Unit =
    s"not bind big decimals that don't match regex: $regex, return ${formErrorToString(expectedError)}" in {

      val validResult   = form.bind(Map(fieldName -> validValue.toString)).apply(fieldName)
      val invalidResult = form.bind(Map(fieldName -> invalidValue.toString)).apply(fieldName)
      validResult.errors must not contain expectedError
      invalidResult.errors must contain only expectedError
    }

}
