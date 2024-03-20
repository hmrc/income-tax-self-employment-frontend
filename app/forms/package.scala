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

package object forms {
  val LessThanZeroError: String = "error.lessThanZero"
  val InvalidError: String      = "error.invalid"
  val NonNumericError: String   = "error.nonNumeric"
  val NoDecimalsError: String   = "error.nonDecimal"
  val OverMaxError: String      = "error.overMax"
  val ErrorBoolean: String      = "error.boolean"
  val ValidDateError: String    = "error.date.valid"
  val MissingDayError: String   = "error.date.day"
  val MissingMonthError: String = "error.date.month"
  val MissingYearError: String  = "error.date.year"

  val PostcodeRegex = """^[A-Z]{1,2}\d[A-Z\d]? ?\d[A-Z]{2}$"""
}
