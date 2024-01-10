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

package controllers.journeys.expenses.irrecoverableDebts

import base.SpecBase
import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import forms.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountFormProvider
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.expenses.irrecoverableDebts.{IrrecoverableDebtsAmountPage, IrrecoverableDebtsDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import services.SelfEmploymentService
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.irrecoverableDebts.IrrecoverableDebtsDisallowableAmountView

import scala.concurrent.Future

class IrrecoverableDebtsDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("FinancialChargesDisallowableAmountController", IrrecoverableDebtsDisallowableAmountPage) {

  lazy val onPageLoadRoute: String = routes.IrrecoverableDebtsDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.IrrecoverableDebtsDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call =
    routes.IrrecoverableDebtsDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode) // TODO: Add CYA nav.

  private val mockService = mock[SelfEmploymentService]

  mockService.persistAnswer(*[BusinessId], *[UserAnswers], *, *)(*) returns Future.successful(pageAnswers)

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)),
    bind[SelfEmploymentService].toInstance(mockService)
  )

  override val emptyUserAnswers: UserAnswers =
    SpecBase.emptyUserAnswers.set(IrrecoverableDebtsAmountPage, amount, businessId.some).success.value

  override def createForm(user: UserType): Form[BigDecimal] = new IrrecoverableDebtsDisallowableAmountFormProvider()(user, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[IrrecoverableDebtsDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, formatMoney(amount)).toString()
  }

}
