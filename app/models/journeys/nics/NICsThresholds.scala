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

object NICsThresholds {

  // TODO remove fallback tax year after https://jira.tools.tax.service.gov.uk/browse/SASS-8881
  //  and add data for all tax years that we support
  private val fallbackYear: Int = 2025

  private case class Class4Limits(lowerProfitsLimit: Int, upperProfitsLimit: Int, rateBetweenLimits: Double, rateAboveUpperLimit: Double)

  private def formatter(number: AnyVal): String = NumberFormat.getNumberInstance.format(number)

  private def getFiguresForYear[T](taxYear: Int, figuresMap: Map[Int, T]): T = figuresMap.getOrElse(taxYear, figuresMap(fallbackYear))

  object Class4NICsFigures {

    private val taxYearFiguresMap: Map[Int, Class4Limits] = Map(
      2022 -> Class4Limits(9568, 50270, 9, 2),
      2023 -> Class4Limits(11908, 50270, 9.73, 2.73),
      2024 -> Class4Limits(12570, 50270, 9, 2),
      2025 -> Class4Limits(12570, 50270, 6, 2)
    )

    def getFiguresForTaxYear(taxYear: TaxYear, figureType: String): String = {
      // TODO refactor to using richer types rather than Int and String if this is to remain
      val class4Figures = getFiguresForYear(taxYear.endYear, taxYearFiguresMap)
      val figure = figureType match {
        case "lowerProfitsLimit"   => class4Figures.lowerProfitsLimit
        case "upperProfitsLimit"   => class4Figures.upperProfitsLimit
        case "rateBetweenLimits"   => class4Figures.rateBetweenLimits
        case "rateAboveUpperLimit" => class4Figures.rateAboveUpperLimit
        case _                     => throw new IllegalArgumentException(s"Invalid figure name: $figureType")
      }
      formatter(figure)
    }
  }

  object Class2NICsThresholds {
    private val taxYearSmallProfitsThresholdMap: Map[Int, Int] = Map(
      2022 -> 6515,
      2023 -> 6725,
      2024 -> 6725,
      2025 -> 6725
    )

    def getThresholdForTaxYear(taxYear: TaxYear): String = {
      val class2Threshold = getFiguresForYear(taxYear.endYear, taxYearSmallProfitsThresholdMap)
      formatter(class2Threshold)
    }
  }
}
