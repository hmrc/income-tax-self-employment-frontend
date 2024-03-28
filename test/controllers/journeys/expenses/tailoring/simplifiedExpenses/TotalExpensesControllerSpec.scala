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

package controllers.journeys.expenses.tailoring.simplifiedExpenses

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.tailoring
import models.NormalMode
import models.database.UserAnswers
import models.journeys.expenses.ExpensesTailoring.TotalAmount
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.tailoring.ExpensesCategoriesPage
import pages.expenses.tailoring.simplifiedExpenses.TotalExpensesPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.tailoring.simplifiedExpenses.TotalExpensesView

import scala.concurrent.Future

class TotalExpensesControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "TotalExpensesController",
      TotalExpensesPage
    ) {

  lazy val onPageLoadRoute: String = routes.TotalExpensesController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.TotalExpensesController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = tailoring.routes.ExpensesTailoringCYAController.onPageLoad(taxYear, businessId)

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )
  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      ExpensesCategoriesPage.toString -> TotalAmount.toString
    )
  )

  when(mockService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(pageAnswers)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[TotalExpensesView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
