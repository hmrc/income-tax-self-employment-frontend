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

package controllers

import base.SpecBase
import builders.BusinessDataBuilder.aBusinessData
import cats.implicits.catsSyntaxEitherId
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ControllersPackageSpec extends SpecBase {

  private val defaultMaxMonths: Int = 12

  "getMaxMonthsWithinTaxYearOrRedirect" - {
    "when there is valid BusinessData" - {
      "should return the defaultMaxMonths as Right when the difference of months between the Commencement date and TaxYear.endYear is 12 or more" in {
        val commencementYear = 2018
        val result           = getMaxMonthsWithinTaxYearOrRedirect(aBusinessData.copy(commencementDate = Some(s"$commencementYear-04-06")), taxYear)
        result shouldBe defaultMaxMonths.asRight
      }
      "should return the number of months between the Commencement date and TaxYear.endYear as Right" in {
        val taxYearCutoffDate = LocalDate.parse(s"${taxYear.endYear}-04-05")
        val commencementDate  = s"${taxYear.endYear}-01-05"
        val expectedResult    = ChronoUnit.MONTHS.between(LocalDate.parse(commencementDate), taxYearCutoffDate).toInt
        val result            = getMaxMonthsWithinTaxYearOrRedirect(aBusinessData.copy(commencementDate = Some(commencementDate)), taxYear)
        result shouldBe expectedResult.asRight
        result shouldBe 3.asRight
      }
    }
    "should return an Error Redirect in a Left" - {
      "when the BusinessData does not have a Commencement date" in {
        val result = getMaxMonthsWithinTaxYearOrRedirect(aBusinessData.copy(commencementDate = None), taxYear)
        result shouldBe redirectJourneyRecovery().asLeft
      }
      "when the Commencement date is later than the TaxYear.endYear" in {
        val result = getMaxMonthsWithinTaxYearOrRedirect(aBusinessData.copy(commencementDate = Some(s"${taxYear.endYear + 1}-04-06")), taxYear)
        result shouldBe redirectJourneyRecovery().asLeft
      }
    }
  }

}
