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

package utils

import java.time.{LocalDate, Month}

object TaxYearUtils { // TODO combine this and NICsThresholds into a TaxYearConstants object if possible

  val dateNow = LocalDate.now()

  val currentTaxYearStartDate =
    if (dateNow.isBefore(LocalDate.of(dateNow.getYear, Month.APRIL, 6))) LocalDate.of(dateNow.getYear - 1, Month.APRIL, 6)
    else LocalDate.of(dateNow.getYear, Month.APRIL, 6)

}
