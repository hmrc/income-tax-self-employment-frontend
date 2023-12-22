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

package models.common

import enumeratum._
import models.journeys.Journey
import models.requests.TradesJourneyStatuses

sealed abstract class JourneyStatus(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}

object JourneyStatus extends Enum[JourneyStatus] with utils.PlayJsonEnum[JourneyStatus] {
  val values = findValues

  case object CheckOurRecords extends JourneyStatus("checkOurRecords")
  case object CannotStartYet  extends JourneyStatus("cannotStartYet")
  case object NotStarted      extends JourneyStatus("notStarted")
  case object InProgress      extends JourneyStatus("inProgress")
  case object Completed       extends JourneyStatus("completed")

  def getJourneyStatus(journey: Journey, journeyStatuses: TradesJourneyStatuses): JourneyStatus =
    statusFromCompletedState(getCompletedState(journey, journeyStatuses))

  def statusFromCompletedState(value: Option[Boolean]): JourneyStatus =
    value.fold[JourneyStatus](NotStarted) {
      case false => InProgress
      case true  => Completed
    }

  private def getCompletedState(journey: Journey, journeyStatuses: TradesJourneyStatuses): Option[Boolean] =
    journeyStatuses.journeyStatuses.find(_.journey == journey).flatMap(_.completedState)

  def tradeDetailsStatusFromCompletedState(value: Option[Boolean]): JourneyStatus =
    value.fold[JourneyStatus](CheckOurRecords) {
      case false => InProgress
      case true  => Completed
    }
}
