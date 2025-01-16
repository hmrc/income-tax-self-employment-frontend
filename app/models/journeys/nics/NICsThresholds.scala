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

import config.TaxYearConfig
import models.common.TaxYear
import models.common.TaxYear.{currentTaxYearStartDate, dateNow}

import java.text.NumberFormat
import java.time.{LocalDate, Period}

object NICsThresholds {

  final case class Class4Limits(lowerProfitsLimit: Int, upperProfitsLimit: Int, rateBetweenLimits: Double, rateAboveUpperLimit: Double)

  private def formatter(number: AnyVal): String = NumberFormat.getNumberInstance.format(number)

  private def getFiguresForYear[T](taxYear: Int, figuresMap: Map[Int, T]): T =
    figuresMap.getOrElse(taxYear, throw new IllegalArgumentException(s"Tax year not supported: $taxYear"))

  object Class4NICsFigures {

    // TODO introduce FigureType enum
    def getFiguresForTaxYear(taxYear: TaxYear, figureType: String): Double = {
      val class4Figures = getFiguresForYear(taxYear.endYear, TaxYearConfig.taxYearFiguresClass4NicsMap)
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

    def getThresholdForTaxYear(taxYear: TaxYear): Int = getFiguresForYear(taxYear.endYear, TaxYearConfig.taxYearSmallProfitsThresholdClass2Map)

    def getThresholdForTaxYearFormatted(taxYear: TaxYear): String = formatter(getThresholdForTaxYear(taxYear))
  }

  object StatePensionAgeThresholds {
    def getThresholdForTaxYear(taxYear: TaxYear): Int = getFiguresForYear(taxYear.endYear, TaxYearConfig.taxYearStatePensionAgeThresholdMap)

    def ageIsUnder16(userDoB: LocalDate, taxYear: TaxYear, ageAtStartOfTaxYear: Boolean): Boolean = {
      val comparisonDayMonth = if (ageAtStartOfTaxYear) currentTaxYearStartDate else dateNow
      val comparisonDate     = LocalDate.of(comparisonDayMonth.getYear, comparisonDayMonth.getMonthValue, comparisonDayMonth.getDayOfMonth)
      val age                = Period.between(userDoB, comparisonDate).getYears
      age < 16
    }
    def ageIsUnderStatePensionAge(userDoB: LocalDate, taxYear: TaxYear, ageAtStartOfTaxYear: Boolean): Boolean = {
      val comparisonDayMonth = if (ageAtStartOfTaxYear) currentTaxYearStartDate else dateNow
      val comparisonDate     = LocalDate.of(comparisonDayMonth.getYear, comparisonDayMonth.getMonthValue, comparisonDayMonth.getDayOfMonth)
      val age                = Period.between(userDoB, comparisonDate).getYears
      val statePensionAge    = StatePensionAgeThresholds.getThresholdForTaxYear(taxYear)
      age < statePensionAge
    }
    def ageIsBetween16AndStatePension(userDoB: LocalDate, taxYear: TaxYear, ageAtStartOfTaxYear: Boolean): Boolean = {
      val is16OrOver             = !ageIsUnder16(userDoB, taxYear, ageAtStartOfTaxYear)
      val isUnderStatePensionAge = ageIsUnderStatePensionAge(userDoB, taxYear, ageAtStartOfTaxYear)

      is16OrOver && isUnderStatePensionAge
    }
  }
}
