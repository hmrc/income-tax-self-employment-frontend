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

package models.journeys.capitalallowances.electricVehicleChargePoints

import models.common.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait EvcpHowMuchDoYouWantToClaim
object EvcpHowMuchDoYouWantToClaim extends Enumerable.Implicits {

  case object FullCost    extends WithName("fullCost") with EvcpHowMuchDoYouWantToClaim
  case object LowerAmount extends WithName("lowerAmount") with EvcpHowMuchDoYouWantToClaim

  val values: Seq[EvcpHowMuchDoYouWantToClaim] = Seq(
    FullCost,
    LowerAmount
  )

  def options(fullCostAmount: String)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"expenses.${value.toString}", fullCostAmount)),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[EvcpHowMuchDoYouWantToClaim] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
