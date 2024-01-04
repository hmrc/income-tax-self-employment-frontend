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

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.financialCharges.FinancialChargesDisallowableAmountFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.expenses.financialCharges.{FinancialChargesAmountPage, FinancialChargesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentServiceBase
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.financialCharges.FinancialChargesDisallowableAmountView

import scala.concurrent.Future

class FinancialChargesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("FinancialChargesDisallowableAmountController", FinancialChargesDisallowableAmountPage) {

  lazy val onPageLoadRoute: String = routes.FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.FinancialChargesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call =
    routes.FinancialChargesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode) // TODO: Add CYA nav.

  private val mockService = mock[SelfEmploymentServiceBase]

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(filledUserAnswers)

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentServiceBase].toInstance(mockService)
  )

  override lazy val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(FinancialChargesAmountPage, amount, businessId.some).success.value

  private lazy val amount = BigDecimal(123.00)

  override def createForm(user: UserType): Form[BigDecimal] = new FinancialChargesDisallowableAmountFormProvider()(user, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[FinancialChargesDisallowableAmountView]
    view(form, scenario.mode, scenario.taxYear, scenario.businessId, scenario.userType, formatMoney(amount)).toString()
  }

}
