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
import models.common.TaxYear.{currentTaxYearStartDate, dateNow}

import java.text.NumberFormat
import java.time.{LocalDate, Period}

// TODO refactor to combine with other year-dependent config into one place
//  get data from a centralised config when available rather than hardcoded
//  also use richer types rather than Int and String if this is to remain

object NICsThresholds {

  private case class Class4Limits(lowerProfitsLimit: Int, upperProfitsLimit: Int, rateBetweenLimits: Double, rateAboveUpperLimit: Double)

  private def formatter(number: AnyVal): String = NumberFormat.getNumberInstance.format(number)

  private def getFiguresForYear[T](taxYear: Int, figuresMap: Map[Int, T]): T =
    figuresMap.getOrElse(taxYear, throw new IllegalArgumentException(s"Tax year not supported: $taxYear"))

  object Class4NICsFigures {

    private val taxYearFiguresMap: Map[Int, Class4Limits] = Map(
      2021 -> Class4Limits(9500, 50000, 9, 2),
      2022 -> Class4Limits(9568, 50270, 9, 2),
      2023 -> Class4Limits(11908, 50270, 9.73, 2.73),
      2024 -> Class4Limits(12570, 50270, 9, 2),
      2025 -> Class4Limits(12570, 50270, 6, 2)
    )

    def getFiguresForTaxYear(taxYear: TaxYear, figureType: String): Double = {
      val class4Figures = getFiguresForYear(taxYear.endYear, taxYearFiguresMap)
      figureType match {
        case "lowerProfitsLimit"   => class4Figures.lowerProfitsLimit
        case "upperProfitsLimit"   => class4Figures.upperProfitsLimit
        case "rateBetweenLimits"   => class4Figures.rateBetweenLimits
        case "rateAboveUpperLimit" => class4Figures.rateAboveUpperLimit
        case _                     => throw new IllegalArgumentException(s"Invalid figure name: $figureType")
      }
    }

    def getFiguresForTaxYearFormatted(taxYear: TaxYear, figureType: String): String = formatter(getFiguresForTaxYear(taxYear, figureType))
  }

  object Class2NICsThresholds {
    private val taxYearSmallProfitsThresholdMap: Map[Int, Int] = Map(
      2021 -> 6475,
      2022 -> 6515,
      2023 -> 6725,
      2024 -> 6725,
      2025 -> 6725
    )

    def getThresholdForTaxYear(taxYear: TaxYear): Int = getFiguresForYear(taxYear.endYear, taxYearSmallProfitsThresholdMap)

    def getThresholdForTaxYearFormatted(taxYear: TaxYear): String = formatter(getThresholdForTaxYear(taxYear))
  }

  object StatePensionAgeThresholds {
    private val taxYearStatePensionAgeThresholdMap: Map[Int, Int] = Map(
      2021 -> 66,
      2022 -> 66,
      2023 -> 66,
      2024 -> 66,
      2025 -> 66
    )

    def getThresholdForTaxYear(taxYear: TaxYear): Int = getFiguresForYear(taxYear.endYear, taxYearStatePensionAgeThresholdMap)

    def ageIsBetween16AndStatePension(userDoB: LocalDate, taxYear: TaxYear, ageAtStartOfTaxYear: Boolean): Boolean = {
      val comparisonDayMonth   = if (ageAtStartOfTaxYear) currentTaxYearStartDate else dateNow
      val comparisonDate       = LocalDate.of(taxYear.endYear, comparisonDayMonth.getMonthValue, comparisonDayMonth.getDayOfMonth)
      val age: Int             = Period.between(userDoB, comparisonDate).getYears
      val statePensionAge: Int = StatePensionAgeThresholds.getThresholdForTaxYear(taxYear)

      16 <= age && age < statePensionAge
    }
  }
}
