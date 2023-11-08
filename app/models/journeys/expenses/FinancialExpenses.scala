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

sealed trait FinancialExpenses

object FinancialExpenses extends Enumerable.Implicits {

  case object Interest              extends WithName("interest") with FinancialExpenses
  case object OtherFinancialCharges extends WithName("otherFinancialCharges") with FinancialExpenses
  case object IrrecoverableDebts    extends WithName("irrecoverableDebts") with FinancialExpenses
  case object NoFinancialExpenses   extends WithName("noFinancialExpenses") with FinancialExpenses
  case object CheckboxDivider       extends WithName("or") with FinancialExpenses

  val values: Seq[FinancialExpenses] = Seq(
    Interest,
    OtherFinancialCharges,
    IrrecoverableDebts,
    CheckboxDivider,
    NoFinancialExpenses
  )

  def checkboxItems(userType: String, accountingType: String)(implicit messages: Messages): Seq[CheckboxItem] = {
    val filteredValues = if (accountingType.equals("CASH")) values.filterNot(_.equals(IrrecoverableDebts)) else values
    filteredValues.zipWithIndex.map {
      case (value, _) if value.equals(CheckboxDivider) => CheckboxItem(divider = Some(CheckboxDivider.toString))
      case (value, index) if value.equals(NoFinancialExpenses) =>
        CheckboxItemViewModel(
          content = Text(messages(s"financialExpenses.${value.toString}.$userType")),
          fieldId = "value",
          index = index,
          value = value.toString
        ).withAttribute(("data-behaviour", "exclusive"))
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"financialExpenses.${value.toString}")),
          fieldId = "value",
          index = index,
          value = value.toString
        )
    }
  }

  implicit val enumerable: Enumerable[FinancialExpenses] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
