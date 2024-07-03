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

import base.SpecBase.businessId
import models.common.{AccountingType, JourneyStatus, TradingName, TypeOfBusiness}
import models.journeys.Journey._
import models.journeys.{JourneyNameAndStatus, TaskList}
import models.requests.TradesJourneyStatuses

object TradesJourneyStatusesBuilder {

  val aTadesJourneyStatusesModel: TradesJourneyStatuses = TradesJourneyStatuses(
    businessId,
    Some(TradingName("TradingName1")),
    TypeOfBusiness("SelfEmployment"),
    AccountingType.Accrual,
    List(
      JourneyNameAndStatus(Abroad, JourneyStatus.Completed),
      JourneyNameAndStatus(Income, JourneyStatus.InProgress),
      JourneyNameAndStatus(ExpensesTailoring, JourneyStatus.CheckOurRecords),
      JourneyNameAndStatus(ExpensesGoodsToSellOrUse, JourneyStatus.CheckOurRecords),
      JourneyNameAndStatus(NationalInsuranceContributions, JourneyStatus.CheckOurRecords)
    )
  )

  val anEmptyTadesJourneyStatusesModel: TradesJourneyStatuses =
    TradesJourneyStatuses(businessId, None, TypeOfBusiness("SelfEmployment"), AccountingType.Accrual, Nil)

  val aSequenceTadesJourneyStatusesModel: List[TradesJourneyStatuses] = List(aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel)

  val aTaskList: TaskList =
    TaskList(
      Some(JourneyNameAndStatus(TradeDetails, JourneyStatus.Completed)),
      List(aTadesJourneyStatusesModel, anEmptyTadesJourneyStatusesModel),
      Nil)

}
