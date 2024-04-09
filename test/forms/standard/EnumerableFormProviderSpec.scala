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

package forms.standard

import base.forms.EnumerableFormProviderBaseSpec
import models.common.UserType
import models.journeys.expenses.individualCategories.OtherExpenses
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.data.Form

class EnumerableFormProviderSpec extends EnumerableFormProviderBaseSpec[OtherExpenses]("RadioButtonFormProvider") {

  override def validValues: Seq[OtherExpenses] = OtherExpenses.values
  override def requiredError                   = "otherExpenses.error.required"
  private def formProvider                     = new EnumerableFormProvider()

  override def getFormProvider(user: UserType): Form[OtherExpenses] = formProvider(OtherExpensesPage, user)
}
