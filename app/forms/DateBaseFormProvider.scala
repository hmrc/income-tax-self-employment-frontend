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

package forms

import forms.mappings.Mappings
import play.api.data.Forms.mapping
import play.api.data.{Form, FormError}

import java.time.LocalDate
import scala.util.Try

abstract class DateBaseFormProvider extends Mappings {

  private val day   = "day"
  private val month = "month"
  private val year  = "year"

  def createForm(earliestDate: LocalDate, dateTooEarlyError: String): Form[DateFormModel] =
    Form[DateFormModel](
      mapping(
        day   -> int(MissingDayError, ValidDateError, ValidDateError),
        month -> int(MissingMonthError, ValidDateError, ValidDateError),
        year  -> int(MissingYearError, ValidDateError, ValidDateError)
      )(DateFormModel.apply)(DateFormModel.unapply)
        .verifying(ValidDateError, form => Try(LocalDate.of(form.year, form.month, form.day)).isSuccess)
        .verifying(dateTooEarlyError, form => Try(LocalDate.of(form.year, form.month, form.day)).getOrElse(LocalDate.now).isAfter(earliestDate))
    )

  def allFieldsEmpty(errorForm: Form[DateFormModel]): Boolean =
    errorForm.errors == Seq(FormError(day, List(MissingDayError)), FormError(month, List(MissingMonthError)), FormError(year, List(MissingYearError)))

  def invalidEntry(errorForm: Form[DateFormModel]): Boolean = errorForm.errors.map(_.messages.contains(ValidDateError)).exists(identity)

  def dateTooEarly(errorForm: Form[DateFormModel], earliestDate: LocalDate): Boolean = errorForm.value.exists(_.toLocalDate.isBefore(earliestDate))
  def dateInFuture(errorForm: Form[DateFormModel], latestDate: LocalDate): Boolean   = errorForm.value.exists(_.toLocalDate.isBefore(latestDate))

}

case class DateFormModel(day: Int, month: Int, year: Int) {
  def toLocalDate: LocalDate = LocalDate.of(year, month, day)
}
object DateFormModel {
  def apply(localDate: LocalDate): DateFormModel = DateFormModel(localDate.getDayOfMonth, localDate.getMonthValue, localDate.getYear)
}
