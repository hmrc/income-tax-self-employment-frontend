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

import enumeratum.{Enum, EnumEntry}

sealed abstract class ProfitOrLoss(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName
}

object ProfitOrLoss extends Enum[ProfitOrLoss] with utils.PlayJsonEnum[ProfitOrLoss] {
  val values = findValues

  case object Profit extends ProfitOrLoss("profit")

  case object Loss extends ProfitOrLoss("loss")

  def returnProfitOrLoss(netAmount: BigDecimal): ProfitOrLoss = if (netAmount < 0) ProfitOrLoss.Loss else ProfitOrLoss.Profit

}
