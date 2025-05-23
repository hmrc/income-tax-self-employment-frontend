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

package models.requests

import config.FrontendAppConfig
import models.common.JourneyStatus.NotStarted
import models.common._
import models.database.UserAnswers
import models.journeys.JourneyNameAndStatus
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import viewmodels.journeys.taskList.PrepopTradeJourneyStatusesViewModel.buildPrepopSummaryList
import viewmodels.journeys.taskList.TradeJourneyStatusesViewModel.buildSummaryList
import viewmodels.journeys.taskList.{PrepopTradeJourneyStatusesViewModel, TradeJourneyStatusesViewModel}

case class TradesJourneyStatuses(businessId: BusinessId,
                                 tradingName: Option[TradingName],
                                 typeOfBusiness: TypeOfBusiness,
                                 accountingType: AccountingType,
                                 journeyStatuses: List[JourneyNameAndStatus]) {
  def getStatusOrNotStarted(journey: Journey): JourneyStatus =
    journeyStatuses.find(_.name == journey).map(_.journeyStatus).getOrElse(NotStarted)
}

object TradesJourneyStatuses {

  implicit val format: OFormat[TradesJourneyStatuses] = Json.format[TradesJourneyStatuses]

  def toViewModel(tradeDetails: TradesJourneyStatuses, taxYear: TaxYear, userAnswers: Option[UserAnswers], appConfig: FrontendAppConfig)(implicit
      message: Messages): TradeJourneyStatusesViewModel =
    TradeJourneyStatusesViewModel(
      tradeDetails.tradingName.getOrElse(TradingName.empty),
      tradeDetails.typeOfBusiness,
      tradeDetails.businessId,
      buildSummaryList(tradeDetails, taxYear, userAnswers, appConfig)
    )

  def toPrepopViewModel(tradeDetails: TradesJourneyStatuses, taxYear: TaxYear)(implicit message: Messages): PrepopTradeJourneyStatusesViewModel =
    PrepopTradeJourneyStatusesViewModel(
      tradeDetails.tradingName.getOrElse(TradingName.empty),
      tradeDetails.typeOfBusiness,
      tradeDetails.businessId,
      buildPrepopSummaryList(tradeDetails, taxYear)
    )
}
