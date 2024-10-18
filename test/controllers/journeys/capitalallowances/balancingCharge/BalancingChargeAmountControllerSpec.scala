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

package controllers.journeys.capitalallowances.balancingCharge

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys
import forms.capitalallowances.balancingCharge.BalancingChargeAmountFormProvider
import models.NormalMode
import models.common.UserType
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import pages.capitalallowances.balancingCharge.BalancingChargeAmountPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.capitalallowances.balancingCharge.BalancingChargeAmountView

class BalancingChargeAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "BalancingChargeAmountController",
      BalancingChargeAmountPage
    ) {

  lazy val onPageLoadRoute = routes.BalancingChargeAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  lazy val onSubmitRoute   = routes.BalancingChargeAmountController.onSubmit(taxYear, businessId, NormalMode).url

  override val onwardRoute = journeys.routes.TaskListController.onPageLoad(taxYear)
  // Change to below when CYA page enabled
  // override val onwardRoute: Call = routes.BalancingChargeCYAController.onPageLoad(taxYear, businessId)

  override val bindings: List[Binding[_]] = List(bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute)))

  override def createForm(userType: UserType): Form[BigDecimal] = new BalancingChargeAmountFormProvider()(userType)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[BalancingChargeAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
