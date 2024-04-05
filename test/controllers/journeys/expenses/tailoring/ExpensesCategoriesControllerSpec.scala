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

package controllers.journeys.expenses.tailoring

import base.questionPages.RadioButtonGetAndPostQuestionBaseSpec
import forms.expenses.tailoring.ExpensesCategoriesFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring
import models.journeys.expenses.ExpensesTailoring.NoExpenses
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.income.TurnoverIncomeAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.ExpensesCategoriesView

import scala.concurrent.Future

class ExpensesCategoriesControllerSpec
    extends RadioButtonGetAndPostQuestionBaseSpec[ExpensesTailoring](
      "ExpensesCategoriesController",
      ExpensesCategoriesPage
    ) {

  private lazy val incomeAmount: BigDecimal       = 80000
  private lazy val incomeThreshold: BigDecimal    = 85000
  private lazy val incomeIsOverThreshold: Boolean = incomeAmount > incomeThreshold

  override def onPageLoadCall: Call           = routes.ExpensesCategoriesController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call             = routes.ExpensesCategoriesController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call              = routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)
  override def validAnswer: ExpensesTailoring = NoExpenses
  override def baseAnswers: UserAnswers       = emptyUserAnswers.set(TurnoverIncomeAmountPage, incomeAmount, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  def createForm(userType: UserType): Form[ExpensesTailoring] = new ExpensesCategoriesFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ExpensesCategoriesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, incomeIsOverThreshold).toString()
  }

}
