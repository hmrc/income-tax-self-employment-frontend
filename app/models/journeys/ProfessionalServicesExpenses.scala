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

package models.journeys

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait ProfessionalServicesExpenses

object ProfessionalServicesExpenses extends Enumerable.Implicits {

  case object Staff            extends WithName("staff") with ProfessionalServicesExpenses
  case object Subcontractors   extends WithName("subcontractors") with ProfessionalServicesExpenses
  case object ProfessionalFees extends WithName("professionalFees") with ProfessionalServicesExpenses
  case object No               extends WithName("no") with ProfessionalServicesExpenses

  val values: Seq[ProfessionalServicesExpenses] = Seq(
    Staff,
    Subcontractors,
    ProfessionalFees,
    No
  )

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map { case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(s"professionalServicesExpenses.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[ProfessionalServicesExpenses] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
