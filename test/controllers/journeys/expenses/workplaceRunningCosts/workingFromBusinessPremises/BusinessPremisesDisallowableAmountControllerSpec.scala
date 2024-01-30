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
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesDisallowableAmountFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.{BusinessPremisesAmountPage, BusinessPremisesDisallowableAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import utils.MoneyUtils.formatMoney
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.BusinessPremisesDisallowableAmountView

class BusinessPremisesDisallowableAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "DisallowableGoodsToSellOrUseAmountController",
      BusinessPremisesDisallowableAmountPage
    ) {

  lazy val onPageLoadRoute: String = routes.BusinessPremisesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute: String   = routes.BusinessPremisesDisallowableAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute: Call = routes.BusinessPremisesDisallowableAmountController.onPageLoad(taxYear, businessId, NormalMode)
  // TODO change to workplace running costs cya

  override def baseAnswers = emptyUserAnswers.set(BusinessPremisesAmountPage, amount, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute))
  )

  def createForm(userType: UserType): Form[BigDecimal] = new BusinessPremisesDisallowableAmountFormProvider()(userType, amount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[BusinessPremisesDisallowableAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, formatMoney(amount)).toString()
  }

}
