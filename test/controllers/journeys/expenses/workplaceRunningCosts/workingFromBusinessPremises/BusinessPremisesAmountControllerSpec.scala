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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxEitherId
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountFormProvider
import models.NormalMode
import models.common.AccountingType.Accrual
import models.common.{BusinessId, Mtditid, Nino, UserType}
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import org.mockito.IdiomaticMockito.StubbingOps
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesAmountView

class BusinessPremisesAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "BusinessPremisesAmountController",
      BusinessPremisesAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.BusinessPremisesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.BusinessPremisesAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override val bindings: List[Binding[_]] =
    List(bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute)))

  mockService.getAccountingType(*[Nino], *[BusinessId], *[Mtditid])(*) returns Accrual.asRight.asFuture

  def createForm(userType: UserType): Form[BigDecimal] = new BusinessPremisesAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[BusinessPremisesAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

}