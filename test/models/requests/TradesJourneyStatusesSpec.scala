/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase._
import cats.implicits._
import models.common.AccountingType.Accrual
import models.common.{Journey, JourneyStatus}
import models.journeys.JourneyNameAndStatus
import org.scalatest.wordspec.AnyWordSpecLike

class TradesJourneyStatusesSpec extends AnyWordSpecLike {

  val statuses = TradesJourneyStatuses(
    businessId,
    tradingName.some,
    typeOfBusiness,
    Accrual,
    List(
      JourneyNameAndStatus(Journey.ExpensesConstruction, JourneyStatus.Completed)
    ))

  "getStatusOrNotStarted" should {
    "return a journey status if exist" in {
      assert(statuses.getStatusOrNotStarted(Journey.ExpensesConstruction) === JourneyStatus.Completed)
    }

    "return NotStarted if Journey does not exist" in {
      assert(statuses.getStatusOrNotStarted(Journey.ExpensesAdvertisingOrMarketing) === JourneyStatus.NotStarted)
    }
  }
}
