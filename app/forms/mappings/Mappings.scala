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
import models.common.{AccountingType, Enumerable, UserType}
import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required",
                     args: Seq[String] = Seq.empty,
                     toUpperCase: Boolean = false,
                     stripWhitespace: Boolean = false): FieldMapping[String] =
    of(stringFormatter(errorKey, args, toUpperCase, stripWhitespace))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric",
                    args: Seq[String] = Seq.empty): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def bigDecimal(requiredKey: String = "error.required",
                           nonNumericKey: String = "error.nonNumeric",
                           args: Seq[String] = Seq.empty): FieldMapping[BigDecimal] =
    of(bigDecimalFormatter(requiredKey, nonNumericKey, args))

  def currency(requiredKey: String = "error.required",
               nonNumericKey: String = "error.nonNumeric",
               args: Seq[String] = Seq.empty): FieldMapping[BigDecimal] =
    of(currencyFormatter(requiredKey, nonNumericKey, args))

  def boolean(requiredKey: String = "error.required", invalidKey: String = "error.boolean", args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def enumerable[A](requiredKey: String = "error.required", invalidKey: String = "error.invalid", args: Seq[String] = Seq.empty)(implicit
      ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(requiredKey: String,
                          invalidKey: String = ValidDateError,
                          missingDay: String = MissingDayError,
                          missingMonth: String = MissingMonthError,
                          missingYear: String = MissingYearError,
                          earliestDateAndError: Option[(LocalDate, String)] = None,
                          latestDateAndError: Option[(LocalDate, String)] = None,
                          args: Seq[String] = Seq.empty): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(requiredKey, invalidKey, missingDay, missingMonth, missingYear, earliestDateAndError, latestDateAndError, args))

  def userTypeAware(userType: UserType, prefix: String): String = s"$prefix.${userType.toString}"
  def optAccountingTypeAware(accountingType: Option[AccountingType])(prefix: String): String =
    if (accountingType.exists(_ == AccountingType.Cash)) s"$prefix.cash" else prefix
}

object Mappings extends Mappings
