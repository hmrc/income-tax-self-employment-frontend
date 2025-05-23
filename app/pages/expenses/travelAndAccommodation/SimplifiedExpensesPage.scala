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

package pages.expenses.travelAndAccommodation

import models.journeys.expenses.travelAndAccommodation.VehicleDetailsDb
import pages.OneQuestionPage

case object SimplifiedExpensesPage extends OneQuestionPage[Boolean] {

  override def toString: String = "simplifiedExpenses"

  def clearDependentPageDataAndUpdate(value: Boolean, oldAnswers: VehicleDetailsDb): VehicleDetailsDb = {
    val needsClear = !oldAnswers.usedSimplifiedExpenses.contains(value)

    oldAnswers.copy(
      calculateFlatRate = if (needsClear) None else oldAnswers.calculateFlatRate,
      expenseMethod = if (needsClear) None else oldAnswers.expenseMethod,
      vehicleExpenses = if (needsClear) None else oldAnswers.vehicleExpenses,
      usedSimplifiedExpenses = Some(value)
    )
  }

}
