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

package controllers.journeys.expenses.construction

import base.BigDecimalGetAndPostQuestionBaseSpec
import models.NormalMode
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.Request
import views.html.journeys.expenses.construction.ConstructionIndustryAmountView
import forms.expenses.construction.ConstructionIndustryAmountFormProvider
import models.common.UserType
import pages.expenses.construction.ConstructionIndustryAmountPage

class ConstructionIndustryAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "ConstructionIndustryAmountController",
      ConstructionIndustryAmountPage
    ) {

  lazy val onPageLoadRoute = routes.ConstructionIndustryAmountController.onPageLoad(taxYear, stubBusinessId, NormalMode).url
  lazy val onSubmitRoute   = routes.ConstructionIndustryAmountController.onSubmit(taxYear, stubBusinessId, NormalMode).url

  override val bindings: List[Binding[_]] = List(bind[ExpensesNavigator].toInstance(FakeExpensesNavigator()))

  def createForm(userType: UserType): Form[BigDecimal] = new ConstructionIndustryAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ConstructionIndustryAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
