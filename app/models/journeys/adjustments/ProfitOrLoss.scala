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

package models.journeys.adjustments

import enumeratum._
import models.common.{Enumerable, WithName}

sealed trait ProfitOrLoss extends EnumEntry {
  override def entryName: String = toString.toLowerCase
}

object ProfitOrLoss extends Enumerable.Implicits {
  val values: Seq[ProfitOrLoss] = Seq(Profit, Loss)

  case object Profit extends WithName("profit") with ProfitOrLoss
  case object Loss   extends WithName("loss") with ProfitOrLoss

  implicit val enumerable: Enumerable[ProfitOrLoss] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
