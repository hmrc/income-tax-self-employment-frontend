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
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import play.api.mvc.Result
import utils.MoneyUtils.formatMoney

case class WfbpFlatRateViewModel(months1Person: String,
                                 amount1Person: String,
                                 months2People: String,
                                 amount2People: String,
                                 months3People: String,
                                 amount3People: String,
                                 flatRate: String)

object WfbpFlatRateViewModel {

  def calculateFlatRate(request: DataRequest[_], businessId: BusinessId): Either[Result, WfbpFlatRateViewModel] = {
    def formatMonths(months: Int): String = if (months == 1) s"$months month" else s"$months months"

    val months1Person = request.getValue(LivingAtBusinessPremisesOnePerson, businessId)
    val months2People = request.getValue(LivingAtBusinessPremisesTwoPeople, businessId)
    val months3People = request.getValue(LivingAtBusinessPremisesThreePlusPeople, businessId)
    (months1Person, months2People, months3People) match {
      case (Some(months1Person), Some(months2People), Some(months3People)) =>
        val amount1Person = months1Person * 350
        val amount2People = months2People * 500
        val amount3People = months3People * 650
        val flatRate      = amount1Person + amount2People + amount3People
        Right(
          WfbpFlatRateViewModel(
            formatMonths(months1Person),
            formatMoney(amount1Person),
            formatMonths(months2People),
            formatMoney(amount2People),
            formatMonths(months3People),
            formatMoney(amount3People),
            formatMoney(flatRate)
          ))
      case _ => Left(redirectJourneyRecovery())
    }
  }

}
