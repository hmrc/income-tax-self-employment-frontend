/*
 * Copyright 2025 HM Revenue & Customs
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

package models.journeys.expenses.travelAndAccommodation

import models.common.{Enumerable, WithName}
import play.api.i18n.Messages
import play.api.libs.json.{JsString, Writes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait YourFlatRateForVehicleExpenses

object YourFlatRateForVehicleExpenses extends Enumerable.Implicits {

  case object Flatrate   extends WithName("FlatRate") with YourFlatRateForVehicleExpenses
  case object Actualcost extends WithName("ActualCosts") with YourFlatRateForVehicleExpenses

  val values: Seq[YourFlatRateForVehicleExpenses] = Seq(
    Flatrate,
    Actualcost
  )

  def options(totalExpenses: String)(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"expenses.${value.toString}", totalExpenses)),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  implicit val writes: Writes[YourFlatRateForVehicleExpenses] =
    Writes[YourFlatRateForVehicleExpenses] { value =>
      JsString(value.toString)
    }

  implicit val enumerable: Enumerable[YourFlatRateForVehicleExpenses] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
