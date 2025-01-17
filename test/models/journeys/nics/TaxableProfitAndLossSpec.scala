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

package models.journeys.nics

import base.SpecBase.taxYear
import builders.BusinessDataBuilder._
import models.common.TaxYear.{currentTaxYearStartDate, dateNow}
import models.journeys.nics.NICsThresholds.StatePensionAgeThresholds.getThresholdForTaxYear
import models.journeys.nics.NicClassExemption.{Class2, Class4, NotEligible}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TaxableProfitAndLossSpec extends AnyFreeSpec with ScalaCheckPropertyChecks {

  private val validDoB        = dateNow.minusYears(20)
  private val statePensionAge = getThresholdForTaxYear(taxYear)

  private val testScenarios = Table(
    ("taxableProfitsAndLosses", "dateOfBirth", "expectedResult"),
    (withLossesTaxableProfitAndLoss, validDoB, Class2),
    (withLossesTaxableProfitAndLoss, dateNow.minusYears(15), NotEligible),
    (withLossesTaxableProfitAndLoss, dateNow.minusYears(statePensionAge), NotEligible),
    (smallProfitTaxableProfitAndLoss, validDoB, Class2),
    (smallProfitTaxableProfitAndLoss, dateNow.minusYears(15), NotEligible),
    (smallProfitTaxableProfitAndLoss, dateNow.minusYears(statePensionAge), NotEligible),
    (mediumProfitTaxableProfitAndLoss, validDoB, NotEligible),
    (mediumProfitTaxableProfitAndLoss, dateNow.minusYears(15), NotEligible),
    (mediumProfitTaxableProfitAndLoss, dateNow.minusYears(statePensionAge), NotEligible),
    (mediumProfitTaxableProfitAndLoss, validDoB, NotEligible),
    (mediumProfitTaxableProfitAndLoss, currentTaxYearStartDate.minusYears(15), NotEligible),
    (mediumProfitTaxableProfitAndLoss, currentTaxYearStartDate.minusYears(statePensionAge), NotEligible),
    (largeProfitTaxableProfitAndLoss, validDoB, Class4),
    (largeProfitTaxableProfitAndLoss, currentTaxYearStartDate.minusYears(15), NotEligible),
    (largeProfitTaxableProfitAndLoss, currentTaxYearStartDate.minusYears(statePensionAge), NotEligible),
    (largeProfitTaxableProfitAndLoss, dateNow.minusYears(statePensionAge), Class4)
  )

  "returnClassTwoOrFourEligible" - {
    "must return the correct class object depending on if the user is Class 2 or 4 eligible or neither" in {
      forAll(testScenarios) { case (taxableProfitsAndLosses, dateOfBirth, expectedResult) =>

        val result = TaxableProfitAndLoss.returnClassTwoOrFourEligible(taxableProfitsAndLosses, dateOfBirth, taxYear)

        assert(result === expectedResult)
      }
    }
  }

  private val class2TestScenarios = Table(
    ("taxableProfitsAndLosses", "expectedResult"),
    (withLossesTaxableProfitAndLoss, true),
    (smallProfitTaxableProfitAndLoss, true),
    (mediumProfitTaxableProfitAndLoss, false),
    (largeProfitTaxableProfitAndLoss, false)
  )

  "areProfitsOrLossClass2Eligible" - {
    "must return true if sum of profits are under threshold or if there are any losses" in {
      forAll(class2TestScenarios) { case (taxableProfitsAndLosses, expectedResult) =>
        val result = TaxableProfitAndLoss.areProfitsOrLossClass2Eligible(taxableProfitsAndLosses, taxYear)

        assert(result === expectedResult)
      }
    }
  }

  private val class4TestScenarios = Table(
    ("taxableProfitsAndLosses", "expectedResult"),
    (withLossesTaxableProfitAndLoss, false),
    (smallProfitTaxableProfitAndLoss, false),
    (mediumProfitTaxableProfitAndLoss, false),
    (largeProfitTaxableProfitAndLoss, true),
    (smallProfitsWithLargeSumTaxableProfitAndLoss, true)
  )

  "areProfitsOverClass4Threshold" - {
    "must return true if sum of profits are over threshold" in {
      forAll(class4TestScenarios) { case (taxableProfitsAndLosses, expectedResult) =>
        val result = TaxableProfitAndLoss.areProfitsOverClass4Threshold(taxableProfitsAndLosses, taxYear)

        assert(result === expectedResult)
      }
    }
  }
}
