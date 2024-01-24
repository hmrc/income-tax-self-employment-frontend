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

package utils

import models.common.TaxYear

import java.time.LocalDate

object TaxYearHelper {
  private val dateNow: LocalDate = LocalDate.now()

  val taxYearCutoffDate: LocalDate      = LocalDate.parse(s"${dateNow.getYear}-04-05")
  val taxYearInt: Int                   = if (dateNow.isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear
  val taxYear: TaxYear                  = TaxYear(taxYearInt)
  val taxYearEOY: TaxYear               = TaxYear(taxYearInt - 1)
  val taxYearEndOfYearMinusOne: TaxYear = TaxYear(taxYearInt - 2)
  val validTaxYearList: Seq[TaxYear]    = Seq(taxYearEndOfYearMinusOne, taxYearEOY, taxYear)
}
