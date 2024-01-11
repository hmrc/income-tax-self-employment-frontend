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

package forms.mappings

import base.SpecBase
import models.common.MoneyBounds
import play.api.data.Form

class FormattersSpec extends SpecBase with Formatters with MoneyBounds with Mappings {

  private val requiredError     = "No input"
  private val nonNumericError   = "Input is not a number"
  private val lessThanZeroError = "Input is zero or less"
  private val overMaxError      = "Input is greater than the maximum"

  private val testForm = new TestForm()

  class TestForm() {
    def apply(): Form[BigDecimal] =
      Form(
        "value" -> currency(requiredError, nonNumericError)
          .verifying(greaterThan(minimumValue, lessThanZeroError))
          .verifying(lessThan(maximumValue, overMaxError))
      )
  }

  private lazy val validTestValue: Seq[(String, BigDecimal)] = Seq(
    ("50,00", BigDecimal(5000.00)),
    (" 23487 923.20 ", BigDecimal(23487923.20)),
    (".13", BigDecimal(0.13)),
    ("0.4", BigDecimal(0.40)),
    ("£50.5", BigDecimal(50.50)),
    ("060.06", BigDecimal(60.06))
  )

  "currencyFormatter" - {
    "should return a formatter of type BigDecimal that" - {
      "binds valid currency values to 2 decimal places, ignoring spaces, commas or £ symbols:" - {
        validTestValue.foreach { value =>
          s"${value._1}" in {
            val testInput = Map("value" -> value._1)
            val actual    = testForm().bind(testInput).value

            actual mustBe Some(value._2)
          }
        }
      }
      "returns a 'Non-Numeric' error if the input" - {
        "has more than 2 decimal places" in {
          val invalidValue = BigDecimal(10.123231)
          val testInput    = Map("value" -> invalidValue.toString())
          val actual       = testForm().bind(testInput).errors.head.messages.head

          actual mustBe nonNumericError
        }
        "cannot be converted to BigDecimal" in {
          val testInput = Map("value" -> "invalid")
          val actual    = testForm().bind(testInput).errors.head.message

          actual mustBe nonNumericError
        }
      }
      "returns a 'Required' error if the input is empty" in {
        val testInput = Map("value" -> "")
        val actual    = testForm().bind(testInput).errors.head.message

        actual mustBe requiredError
      }
      "returns a 'OverMax' error if the input is greater than the maximum" in {
        val testInput = Map("value" -> (maximumValue + 1).toString())
        val actual    = testForm().bind(testInput).errors.head.message

        actual mustBe overMaxError
      }
      "returns a 'LessThanZero' error if the input is zero or less" in {
        val testInput1 = Map("value" -> zeroValue.toString())
        val actual1    = testForm().bind(testInput1).errors.head.message

        val testInput2 = Map("value" -> (zeroValue - 1).toString())
        val actual2    = testForm().bind(testInput2).errors.head.message

        actual1 mustBe lessThanZeroError
        actual2 mustBe lessThanZeroError
      }
    }
  }
}
