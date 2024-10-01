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

package viewmodels.checkAnswers.expenses.workplaceRunningCosts

import controllers.redirectJourneyRecovery
import models.common.BusinessId
import models.requests.DataRequest
import pages.expenses.workplaceRunningCosts.workingFromHome.{WorkingFromHomeHours101Plus, WorkingFromHomeHours25To50, WorkingFromHomeHours51To100}
import play.api.mvc.Result
import utils.MoneyUtils.formatMoney

case class WfhFlatRateViewModel(months25To50: String,
                                amount25To50: String,
                                months51To100: String,
                                amount51To100: String,
                                months101Plus: String,
                                amount101Plus: String,
                                flatRate: BigDecimal)

object WfhFlatRateViewModel {

  def calculateFlatRate(request: DataRequest[_], businessId: BusinessId): Either[Result, WfhFlatRateViewModel] = {
    def formatMonths(months: Int): String = if (months == 1) s"$months month" else s"$months months"

    val months25To50  = request.getValue(WorkingFromHomeHours25To50, businessId)
    val months51To100 = request.getValue(WorkingFromHomeHours51To100, businessId)
    val months101Plus = request.getValue(WorkingFromHomeHours101Plus, businessId)
    (months25To50, months51To100, months101Plus) match {
      case (Some(months25To50), Some(months51To100), Some(months101Plus)) =>
        val amount25To50  = months25To50 * 10
        val amount51To100 = months51To100 * 18
        val amount101Plus = months101Plus * 26
        val flatRate      = amount25To50 + amount51To100 + amount101Plus
        Right(
          WfhFlatRateViewModel(
            formatMonths(months25To50),
            formatMoney(amount25To50),
            formatMonths(months51To100),
            formatMoney(amount51To100),
            formatMonths(months101Plus),
            formatMoney(amount101Plus),
            flatRate
          ))
      case _ =>
        Left(redirectJourneyRecovery(Some(s"Missing months value for 25-50 ($months25To50), 51-100 ($months51To100) or 101+ ($months101Plus) hours")))
    }
  }

}
