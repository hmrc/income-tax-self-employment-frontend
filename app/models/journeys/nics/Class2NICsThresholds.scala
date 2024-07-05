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

package models.journeys.nics

import models.common.TaxYear

import java.text.NumberFormat

// TODO refactor/remove when CID response is integrated
object Class2NICsThresholds {
  val thresholdsMap: Map[Int, Int] = Map(
    2021 -> 6475,
    2022 -> 6515,
    2023 -> 6725,
    2024 -> 6725,
    2025 -> 6725
  )

  def getThresholdForTaxYear(taxYear: TaxYear): String = {
    val threshold = thresholdsMap.getOrElse(taxYear.endYear, thresholdsMap(2024))
    NumberFormat.getNumberInstance.format(threshold)
  }
}
