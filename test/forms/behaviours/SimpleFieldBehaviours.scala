/*
 * Copyright 2024 HM Revenue & Customs
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

import forms._
import models.common.MoneyBounds._
import models.common.UserType
import models.common.UserType._
import play.api.data.{Form, FormError}

abstract class SimpleFieldBehaviours extends BigDecimalFieldBehaviours with BooleanFieldBehaviours {
  val userTypes = List(Individual, Agent)

  def checkValidValue[A](value: A)(implicit form: Form[_]): Unit =
    s"check valid value: $value" in {
      val result = form.bind(Map(fieldName -> value.toString())).apply(fieldName)
      result.errors mustBe empty
      result.value.value mustBe value.toString()
    }

  def checkValidInstance(instance: Product)(implicit form: Form[_]): Unit =
    s"check valid instance: $instance" in {
      val instanceMap: Map[String, String] = (instance.productElementNames zip instance.productIterator.map(_.toString)).toMap
      val result                           = form.bind(instanceMap)
      result.errors mustBe empty
      result.value.value mustBe instance
    }

  def checkInvalidBoolean(expectedError: String = ErrorBoolean, expectedInvalidKeyArgs: List[String] = Nil)(implicit form: Form[_]): Unit =
    behave like booleanInvalidField(
      form,
      fieldName,
      invalidError = FormError(fieldName, expectedError, args = expectedInvalidKeyArgs)
    )

  def checkMandatoryForm(expectedErrors: FormError*)(implicit form: Form[_]): Unit =
    behave like mandatoryForm(form, expectedErrors: _*)

  def checkMandatoryField(expectedError: String)(implicit form: Form[_]): Unit =
    behave like mandatoryField(form, fieldName, FormError(fieldName, expectedError))

  def checkLessThanField(expectedError: String = LessThanZeroError)(implicit form: Form[_]): Unit =
    behave like lessThanField(form, fieldName, minimumValue, expectedError = FormError(fieldName, expectedError, List(0)))

  def checkNonNumericField(expectedError: String = NonNumericError)(implicit form: Form[_]): Unit =
    behave like bigDecimalField(form, fieldName, FormError(fieldName, expectedError))

  def checkMaximumField(expectedError: String = OverMaxError)(implicit form: Form[_]): Unit =
    behave like bigDecimalFieldWithMaximum(form, fieldName, maximumValue, FormError(fieldName, expectedError, Seq(maximumValue)))

  def checkRegexp(expectedError: String = NoDecimalsError)(implicit form: Form[_]): Unit =
    behave like bigDecimalFieldWithRegex(
      form,
      fieldName,
      noDecimalRegexp,
      BigDecimal(400),
      BigDecimal(400.20),
      FormError(fieldName, expectedError, Seq(noDecimalRegexp)))

  def checkValidBoolean()(implicit form: Form[_]): Unit = {
    checkValidValue(true)
    checkValidValue(false)
  }

  def forAllUserTypes(assertions: UserType => Unit) =
    userTypes.foreach { userType =>
      s"for user $userType" - {
        assertions(userType)
      }
    }

}
