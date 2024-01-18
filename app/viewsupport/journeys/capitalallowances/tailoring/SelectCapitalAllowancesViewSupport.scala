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

package viewsupport.journeys.capitalallowances.tailoring

import models.journeys.capitalallowances.tailoring._
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, Checkboxes}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import viewmodels.govuk.checkbox._
import viewmodels.govuk.fieldset._

object SelectCapitalAllowancesViewSupport {

  val allowanceGroups: List[AllowanceType] =
    List(ZeroEmissionsGroup, StructuresAndBuildingsGroup, AssetAndAllowancesGroup)

  def buildCheckboxItems(allowances: List[CapitalAllowances])(implicit messages: Messages): List[CheckboxItem] =
    allowances.zipWithIndex
      .map { case (allowance, i) =>
        CheckboxItemViewModel(
          content = Text(messages(s"selectCapitalAllowances.$allowance")),
          fieldId = "value",
          index = i,
          value = allowance.toString
        ).withHint(Hint(content = Text(messages(s"selectCapitalAllowances.subText.$allowance"))))
      }

  def buildCheckboxes(items: List[CheckboxItem], content: Html, form: Form[_])(implicit messages: Messages): Checkboxes =
    CheckboxesViewModel(
      form = form,
      name = "value",
      items = items,
      legend = LegendViewModel(HtmlContent(content))
        .withCssClass("govuk-fieldset__legend govuk-fieldset__legend--m")
    )

}
