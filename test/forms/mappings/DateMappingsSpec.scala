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

package forms.mappings

import forms.{MissingDayError, MissingMonthError, MissingYearError, ValidDateError}
import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

import java.time.LocalDate

class DateMappingsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with Mappings {

  val form = Form(
    "value" -> localDate(
      requiredKey = "error.required"
    )
  )

  val validData = datesBetween(
    min = LocalDate.of(2000, 1, 1),
    max = LocalDate.of(3000, 1, 1)
  )

  val invalidField: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

  val missingField: Gen[Option[String]] = Gen.option(Gen.const(""))

  "must bind valid data" in {

    forAll(validData -> "valid date") { date =>
      val data = Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.value.value mustEqual date
    }
  }

  "must fail to bind an empty date" in {

    val result = form.bind(Map.empty[String, String])

    result.errors must contain only FormError("value", "error.required", List.empty)
  }

  "must fail to bind a date that is earlier or later than the specified date boundaries" in {
    val earliestDate = LocalDate.of(2010, 1, 1)
    val latestDate   = LocalDate.of(2020, 1, 1)

    val form = Form(
      "value" -> localDate(
        requiredKey = "error.required",
        earliestDateAndError = Some((earliestDate, "error.dateTooEarly")),
        latestDateAndError = Some((latestDate, "error.dateTooLate"))
      )
    )

    val tooEarlyData = Map(
      "value.day"   -> "1",
      "value.month" -> "1",
      "value.year"  -> "2000"
    )
    val tooLateData = Map(
      "value.day"   -> "1",
      "value.month" -> "1",
      "value.year"  -> "2050"
    )

    val tooEarlyResult = form.bind(tooEarlyData)
    val tooLateResult  = form.bind(tooLateData)

    tooEarlyResult.errors must contain(
      FormError("value", "error.dateTooEarly", List.empty)
    )
    tooLateResult.errors must contain(
      FormError("value", "error.dateTooLate", List.empty)
    )
  }

  "must fail to bind a date with a missing day" in {

    forAll(validData -> "valid date", missingField -> "missing field") { (date, field) =>
      val initialData = Map(
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

      val data = field.fold(initialData) { value =>
        initialData + ("value.day" -> value)
      }

      val result = form.bind(data)

      result.errors must contain only FormError("value", MissingDayError, List.empty)
    }
  }

  "must fail to bind a date with an invalid day" in {

    forAll(validData -> "valid date", invalidField -> "invalid field") { (date, field) =>
      val data = Map(
        "value.day"   -> field,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("value", ValidDateError, List.empty)
      )
    }
  }

  "must fail to bind a date with a missing month" in {

    forAll(validData -> "valid date", missingField -> "missing field") { (date, field) =>
      val initialData = Map(
        "value.day"  -> date.getDayOfMonth.toString,
        "value.year" -> date.getYear.toString
      )

      val data = field.fold(initialData) { value =>
        initialData + ("value.month" -> value)
      }

      val result = form.bind(data)

      result.errors must contain only FormError("value", MissingMonthError, List.empty)
    }
  }

  "must fail to bind a date with an invalid month" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") { (date, field) =>
      val data = Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> field,
        "value.year"  -> date.getYear.toString
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("value", ValidDateError, List.empty)
      )
    }
  }

  "must fail to bind a date with a missing year" in {

    forAll(validData -> "valid date", missingField -> "missing field") { (date, field) =>
      val initialData = Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString
      )

      val data = field.fold(initialData) { value =>
        initialData + ("value.year" -> value)
      }

      val result = form.bind(data)

      result.errors must contain only FormError("value", MissingYearError, List.empty)
    }
  }

  "must fail to bind a date with an invalid year" in {

    forAll(validData -> "valid data", invalidField -> "invalid field") { (date, field) =>
      val data = Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> field
      )

      val result = form.bind(data)

      result.errors must contain(
        FormError("value", ValidDateError, List.empty)
      )
    }
  }

  "must fail to bind an invalid date" in {

    val data = Map(
      "value.day"   -> "30",
      "value.month" -> "2",
      "value.year"  -> "2018"
    )

    val result = form.bind(data)

    result.errors must contain(
      FormError("value", ValidDateError, List.empty)
    )
  }

  "must unbind a date" in {

    forAll(validData -> "valid date") { date =>
      val filledForm = form.fill(date)

      filledForm("value.day").value.value mustEqual date.getDayOfMonth.toString
      filledForm("value.month").value.value mustEqual date.getMonthValue.toString
      filledForm("value.year").value.value mustEqual date.getYear.toString
    }
  }
}
