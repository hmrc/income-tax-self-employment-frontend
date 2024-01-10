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

package controllers.journeys.expenses.financialCharges

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import forms.expenses.financialCharges.FinancialChargesAmountFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.Mockito.when
import pages.expenses.financialCharges.FinancialChargesAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import views.html.journeys.expenses.financialCharges.FinancialChargesAmountView

import scala.concurrent.Future

class FinancialChargesAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("FinancialChargesAmountController", FinancialChargesAmountPage) {

  lazy val onPageLoadRoute: String = routes.FinancialChargesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.FinancialChargesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  private val mockSelfEmploymentService = mock[SelfEmploymentService]

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockSelfEmploymentService)
  )

  when(mockSelfEmploymentService.persistAnswer(anyBusinessId, anyUserAnswers, any, any)(any)) thenReturn Future.successful(filledUserAnswers)

  override def createForm(user: UserType): Form[BigDecimal] = new FinancialChargesAmountFormProvider()(user)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[FinancialChargesAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
