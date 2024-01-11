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

package controllers.journeys.expenses.otherExpenses

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.otherExpenses.OtherExpensesDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.otherExpenses.{OtherExpensesAmountPage, OtherExpensesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.otherExpenses.OtherExpensesDisallowableAmountView

class OtherExpensesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("OtherExpensesDisallowableAmountController", OtherExpensesDisallowableAmountPage) {

  lazy val onPageLoadRoute: String = routes.OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.OtherExpensesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override lazy val onwardRoute: Call = routes.OtherExpensesCYAController.onPageLoad(taxYear, businessId)

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  override def baseAnswers = emptyUserAnswers.set(OtherExpensesAmountPage, amount, businessId.some).success.value

  override def createForm(userType: UserType): Form[BigDecimal] = new OtherExpensesDisallowableAmountFormProvider()(userType, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OtherExpensesDisallowableAmountView]
    view(form, scenario.mode, scenario.taxYear, scenario.businessId, scenario.userType, formatMoney(amount)).toString()
  }

}
