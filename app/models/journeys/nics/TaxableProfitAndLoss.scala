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
import models.domain.BusinessIncomeSourcesSummary
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds.ageIsBetween16AndStatePension
import models.journeys.nics.NICsThresholds.{Class2NICsThresholds, Class4NICsFigures}
import models.journeys.nics.NicClassExemption.{Class2, Class4, NotEligible}
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class TaxableProfitAndLoss(taxableProfit: BigDecimal, taxableLoss: BigDecimal)

object TaxableProfitAndLoss {
  implicit val formats: Format[TaxableProfitAndLoss] = Json.format[TaxableProfitAndLoss]

  def fromBusinessIncomeSourcesSummary(biss: BusinessIncomeSourcesSummary): TaxableProfitAndLoss =
    TaxableProfitAndLoss(biss.taxableProfit, biss.taxableLoss)

  def returnClassTwoOrFourEligible(taxableProfitsAndLosses: List[TaxableProfitAndLoss], userDoB: LocalDate, taxYear: TaxYear): NicClassExemption = {

    def class2Eligible: Boolean = {
      val class2Threshold       = Class2NICsThresholds.getThresholdForTaxYear(taxYear)
      val profitsUnderThreshold = taxableProfitsAndLosses.map(_.taxableProfit).sum < BigDecimal(class2Threshold)
      val hasAnyLosses          = taxableProfitsAndLosses.map(_.taxableLoss).sum != 0
      val ageIsValid            = ageIsBetween16AndStatePension(userDoB, taxYear, ageAtStartOfTaxYear = false)
      ageIsValid && (profitsUnderThreshold || hasAnyLosses)
    }

    def class4Eligible: Boolean = {
      val class4Threshold      = Class4NICsFigures.getFiguresForTaxYear(taxYear, figureType = "lowerProfitsLimit")
      val profitsOverThreshold = taxableProfitsAndLosses.map(_.taxableProfit).sum > BigDecimal(class4Threshold)
      val ageIsValid           = ageIsBetween16AndStatePension(userDoB, taxYear, ageAtStartOfTaxYear = true)
      ageIsValid && profitsOverThreshold
    }

    if (class4Eligible) Class4 else if (class2Eligible) Class2 else NotEligible
  }
}
