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

package controllers.journeys.expenses.professionalFees

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.AccountingType.Accrual
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.professionalFees.ProfessionalFeesAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.professionalFees.ProfessionalFeesAmountView

class ProfessionalFeesAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "ProfessionalFeesAmountController",
      ProfessionalFeesAmountPage
    ) {

  lazy val onPageLoadRoute = routes.ProfessionalFeesAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.ProfessionalFeesAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.ProfessionalFeesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ProfessionalFeesAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, Accrual).toString()
  }

}
