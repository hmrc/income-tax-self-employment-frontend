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

import forms.FormSpec
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {
  val fieldName: String = "value"

  def fieldThatBindsValidData(form: Form[_], fieldName: String, validDataGenerator: Gen[String]): Unit =
    "bind valid data" in {
      forAll(validDataGenerator -> "validDataItem") { dataItem: String =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.value.value mustBe dataItem
        result.errors mustBe empty
      }
    }

  def mandatoryForm(form: Form[_], requiredErrors: FormError*): Unit =
    s"not bind when key is not present, return ${formErrorToString(requiredErrors: _*)}}" in {
      form.bind(emptyForm).errors.toList mustEqual requiredErrors.toList
    }

  def mandatoryField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {
    s"not bind when key is not present, return ${formErrorToString(requiredError)}}" in {
      form.bind(emptyForm).apply(fieldName).errors mustEqual Seq(requiredError)
    }

    s"not bind blank values, return ${formErrorToString(requiredError)}}" in {
      form.bind(Map(fieldName -> "")).apply(fieldName).errors mustEqual Seq(requiredError)
    }
  }

  def lessThanField(form: Form[_], fieldName: String, actual: BigDecimal, expectedError: FormError): Unit =
    s"not bind when value less than minimum, return ${formErrorToString(expectedError)}}" in {
      val result = form.bind(Map(fieldName -> (actual - 1).toString())).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }

  def formErrorToString(formErrors: FormError*): String =
    formErrors match {
      case formError :: Nil => s"${formError.key}=${formError.message}, args=${formError.args.mkString(",")}"
      case errors           => errors.mkString(",")
    }

}

object FieldBehaviours extends FieldBehaviours
