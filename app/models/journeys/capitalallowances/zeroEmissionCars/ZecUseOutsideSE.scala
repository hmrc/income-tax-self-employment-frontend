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

package models.journeys.capitalallowances.zeroEmissionCars

import models.common.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ZecUseOutsideSE
object ZecUseOutsideSE extends Enumerable.Implicits {

  case object Ten             extends WithName("10%") with ZecUseOutsideSE
  case object TwentyFive      extends WithName("25%") with ZecUseOutsideSE
  case object Fifty           extends WithName("50%") with ZecUseOutsideSE
  case object DifferentAmount extends WithName("A different amount") with ZecUseOutsideSE

  val values: Seq[ZecUseOutsideSE] = Seq(
    Ten,
    TwentyFive,
    Fifty,
    DifferentAmount
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[ZecUseOutsideSE] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
