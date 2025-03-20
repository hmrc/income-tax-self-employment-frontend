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

import models.common.{Enumerable, UserType, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait TravelAndAccommodationExpenseType

object TravelAndAccommodationExpenseType extends Enumerable.Implicits {

  case object MyOwnVehicle                         extends WithName("myOwnVehicle") with TravelAndAccommodationExpenseType
  case object LeasedVehicles                       extends WithName("leasedVehicles") with TravelAndAccommodationExpenseType
  case object PublicTransportAndOtherAccommodation extends WithName("publicTransportAndOtherAccommodation") with TravelAndAccommodationExpenseType

  val values: Seq[TravelAndAccommodationExpenseType] = Seq(
    MyOwnVehicle,
    LeasedVehicles,
    PublicTransportAndOtherAccommodation
  )

  def checkboxItems(implicit messages: Messages, userType: UserType): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      val messageKey = value.toString match {
        case "myOwnVehicle" => s"travelAndAccommodationExpenseType.${value.toString}.$userType"
        case _              => s"travelAndAccommodationExpenseType.${value.toString}.common"
      }
      CheckboxItemViewModel(
        content = Text(messages(messageKey)),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[TravelAndAccommodationExpenseType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
