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

package config

import models.journeys.nics.NICsThresholds.Class4Limits

/** Put tax year specific configs in this package.
  *
  * IMPORTANT: It is essential to understand how many tax years we are supporting. Each new tax year we need to update the below values with correct
  * values.
  */
object TaxYearConfig {

  /** if turnover >= incomeThreshold, then the user must use categories for expenses */
  val incomeThreshold: BigDecimal                                          = 85000
  def totalIncomeIsEqualOrAboveThreshold(totalIncome: BigDecimal): Boolean = totalIncome >= incomeThreshold

  val taxYearFiguresClass4NicsMap: Map[Int, Class4Limits] = Map(
    2021 -> Class4Limits(9500, 50000, 9, 2),
    2022 -> Class4Limits(9568, 50270, 9, 2),
    2023 -> Class4Limits(11908, 50270, 9.73, 2.73),
    2024 -> Class4Limits(12570, 50270, 9, 2),
    2025 -> Class4Limits(12570, 50270, 6, 2)
  )

  val taxYearSmallProfitsThresholdClass2Map: Map[Int, Int] = Map(
    2021 -> 6475,
    2022 -> 6515,
    2023 -> 6725,
    2024 -> 6725,
    2025 -> 6725
  )

  val taxYearStatePensionAgeThresholdMap: Map[Int, Int] = Map(
    2021 -> 66,
    2022 -> 66,
    2023 -> 66,
    2024 -> 66,
    2025 -> 66
  )

}
