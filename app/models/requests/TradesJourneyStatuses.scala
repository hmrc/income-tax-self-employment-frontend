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

import models.common.TaxYear
import models.journeys.Journey
import models.requests.TradesJourneyStatuses.JourneyStatus
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import viewmodels.TradeJourneyStatusesViewModel
import viewmodels.TradeJourneyStatusesViewModel.buildSummaryList

case class TradesJourneyStatuses(businessId: String, tradingName: Option[String], journeyStatuses: Seq[JourneyStatus])

object TradesJourneyStatuses {

  implicit val format: OFormat[TradesJourneyStatuses] = Json.format[TradesJourneyStatuses]

  def toViewModel(tradeDetails: TradesJourneyStatuses, taxYear: TaxYear)(implicit message: Messages): TradeJourneyStatusesViewModel =
    TradeJourneyStatusesViewModel(
      if (tradeDetails.tradingName.isEmpty) "" else s"${tradeDetails.tradingName.get} - ",
      tradeDetails.businessId,
      buildSummaryList(tradeDetails, taxYear)
    )

  case class JourneyStatus(journey: Journey, completedState: Option[Boolean])

  object JourneyStatus {
    implicit val format: OFormat[JourneyStatus] = Json.format[JourneyStatus]
  }

}
