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

package controllers.journeys.expenses.otherExpenses

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxEitherId
import forms.expenses.otherExpenses.OtherExpensesAmountFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{AccountingType, BusinessId, UserType}
import models.journeys.expenses.individualCategories.OtherExpenses
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.expenses.otherExpenses.OtherExpensesAmountPage
import pages.expenses.tailoring.individualCategories.OtherExpensesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.Request
import views.html.journeys.expenses.otherExpenses.OtherExpensesAmountView

class OtherExpensesAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "OtherExpensesAmountController",
      OtherExpensesAmountPage
    ) {

  lazy val onPageLoadRoute = routes.OtherExpensesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.OtherExpensesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute = routes.OtherExpensesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override def baseAnswers = emptyUserAnswers.set(OtherExpensesPage, OtherExpenses.YesDisallowable, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  mockService.getAccountingType(*, *[BusinessId], *)(*).value returns Accrual.asRight.asFuture

  override def createForm(user: UserType): Form[BigDecimal] = new OtherExpensesAmountFormProvider()(user)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[OtherExpensesAmountView]
    view(form, scenario.mode, scenario.userType, AccountingType.Accrual, OtherExpenses.YesDisallowable, scenario.taxYear, scenario.businessId)
      .toString()
  }

}
