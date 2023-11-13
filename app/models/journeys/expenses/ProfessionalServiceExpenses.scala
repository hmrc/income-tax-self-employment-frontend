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

package models.journeys.expenses

import models.common.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait ProfessionalServiceExpenses

object ProfessionalServiceExpenses extends Enumerable.Implicits {

  case object Staff            extends WithName("staff") with ProfessionalServiceExpenses
  case object Construction     extends WithName("construction") with ProfessionalServiceExpenses
  case object ProfessionalFees extends WithName("professional.fees") with ProfessionalServiceExpenses
  case object No               extends WithName("no") with ProfessionalServiceExpenses
  case object CheckboxDivider       extends WithName("or") with ProfessionalServiceExpenses

  val values: Seq[ProfessionalServiceExpenses] = Seq(
    Staff,
    Construction,
    ProfessionalFees,
    CheckboxDivider,
    No
  )

  def checkboxItems(userType: String)(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {

      case (value, _) if value.equals(CheckboxDivider) => CheckboxItem(divider = Some(CheckboxDivider.toString))
      case (value, index) if value.equals(No) =>
        CheckboxItemViewModel(
          content = Text(messages(s"professionalServiceExpenses.${value.toString}.$userType")),
          fieldId = "value",
          index = index,
          value = value.toString
        ).withAttribute(("data-behaviour", "exclusive"))
      case (value, index) =>
      CheckboxItemViewModel(
        content = Text(messages(if (value == No) "site.no" else s"professionalServiceExpenses.${value.toString}")),
        fieldId = "value",
        index = index,
        value = value.toString
      )
    }

  implicit val enumerable: Enumerable[ProfessionalServiceExpenses] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
