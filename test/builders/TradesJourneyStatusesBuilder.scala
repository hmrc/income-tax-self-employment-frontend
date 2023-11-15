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

package builders

import models.journeys.Journey.{Abroad, ExpensesGoodsToSellOrUse, ExpensesTailoring, Income, NationalInsurance}
import models.requests.TradesJourneyStatuses
import models.requests.TradesJourneyStatuses.JourneyStatus

object TradesJourneyStatusesBuilder {

  val aTadesJourneyStatusesModel = TradesJourneyStatuses("BusinessId1", Some("TradingName1"), Seq(
    JourneyStatus(Abroad, Some(true)),
    JourneyStatus(Income, Some(false)),
    JourneyStatus(ExpensesTailoring, None),
    JourneyStatus(ExpensesGoodsToSellOrUse, None),
    JourneyStatus(NationalInsurance, None)
  ))

  val anEmptyTadesJourneyStatusesModel = TradesJourneyStatuses("BusinessId2", None, Seq.empty)

  val aSequenceTadesJourneyStatusesModel = Seq(aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel)

}
