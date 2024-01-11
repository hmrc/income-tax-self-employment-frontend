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

package models.journeys

import models.common.JourneyStatus._
import models.common.{Enumerable, JourneyStatus, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import models.journeys.CompletedSectionState.{Yes, No}

sealed trait CompletedSectionState {

  def toStatus: JourneyStatus = this match {
    case Yes => Completed
    case No  => InProgress
  }
}

object CompletedSectionState extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with CompletedSectionState
  case object No  extends WithName("no") with CompletedSectionState

  val values: Seq[CompletedSectionState] = Seq(
    Yes,
    No
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"sectionCompletedState.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[CompletedSectionState] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
