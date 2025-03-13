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
import uk.gov.hmrc.govukfrontend.views.Aliases.{CheckboxItem, Text}
import viewmodels.govuk.all.HintViewModel
import viewmodels.govuk.checkbox._

sealed trait TravelAndAccommodationExpenseType

object TravelAndAccommodationExpenseType extends Enumerable.Implicits {

  private case object MyOwnVehicle    extends WithName("myOwnVehicle") with TravelAndAccommodationExpenseType
  private case object LeasedVehicles extends WithName("leasedVehicles") with TravelAndAccommodationExpenseType
  private case object PublicTransportAndOtherAccommodation extends WithName("publicTransportAndOtherAccommodation") with TravelAndAccommodationExpenseType

  val values: Seq[TravelAndAccommodationExpenseType] = Seq(
    MyOwnVehicle,
    LeasedVehicles,
    PublicTransportAndOtherAccommodation
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"travelAndAccommodationExpenseType.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      ).withHint(HintViewModel(Text(messages(s"travelAndAccommodationExpenseType.${value.toString}.hint"))))
    }

  implicit val enumerable: Enumerable[TravelAndAccommodationExpenseType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
