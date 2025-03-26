/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.expenses.travelAndAccommodation

import controllers.journeys.expenses.travelAndAccommodation.stripTrailingZeros
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.buildKeyValueRow

object TravelMileageSummaryViewModel {

  private val overTheLimitPrice: Double = 0.25
  private val limitPrice: Double        = 0.45
  private val mileageLimit: Int         = 10000

  def buildSummaryList(workMileage: BigDecimal)(implicit messages: Messages): SummaryList = {

    def standardLimitRow(mileage: BigDecimal, limit: BigDecimal) = buildKeyValueRow(
      s"yourFlatRateForVehicleExpenses.c1.45p",
      s"yourFlatRateForVehicleExpenses.c2.45p",
      optKeyArgs = Seq(stripTrailingZeros(mileage)),
      optValueArgs = Seq(limit.toString())
    )

    val rows = if (workMileage > mileageLimit) {
      val aboveMileage     = workMileage - mileageLimit
      val aboveLimitAmount = aboveMileage * overTheLimitPrice
      val limitAmount      = mileageLimit * limitPrice

      def aboveLimitRow(aboveMileage: BigDecimal, aboveLimit: BigDecimal) = buildKeyValueRow(
        s"yourFlatRateForVehicleExpenses.c1.25p",
        s"yourFlatRateForVehicleExpenses.c2.25p",
        optKeyArgs = Seq(stripTrailingZeros(aboveMileage)),
        optValueArgs = Seq(aboveLimit.toString())
      )

      Seq(standardLimitRow(mileageLimit, limitAmount), aboveLimitRow(aboveMileage, aboveLimitAmount))
    } else {
      val limit = workMileage * limitPrice
      Seq(standardLimitRow(workMileage, limit))
    }

    SummaryList(rows).copy(classes = "govuk-summary-list--half")
  }

  def totalFlatRateExpense(workMileage: BigDecimal): BigDecimal =
    if (workMileage > mileageLimit) {
      val aboveLimitMileage = workMileage - mileageLimit
      val aboveLimitAmount  = aboveLimitMileage * overTheLimitPrice
      val limitAmount       = mileageLimit * limitPrice
      limitAmount + aboveLimitAmount
    } else {
      workMileage * limitPrice
    }
}
