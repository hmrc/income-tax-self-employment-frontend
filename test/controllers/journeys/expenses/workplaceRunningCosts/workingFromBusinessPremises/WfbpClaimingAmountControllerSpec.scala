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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpClaimingAmountFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{FakeWorkplaceRunningCostsNavigator, WorkplaceRunningCostsNavigator}
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises.{BusinessPremisesAmountPage, WfbpClaimingAmountPage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.WfbpClaimingAmountView

class WfbpClaimingAmountControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec(
      "WfbpClaimingAmountController",
      WfbpClaimingAmountPage
    ) {

  private lazy val expensesAmount: BigDecimal = 300

  override def onPageLoadRoute: String = routes.WfbpClaimingAmountController.onPageLoad(taxYear, businessId, NormalMode).url
  override def onSubmitRoute: String   = routes.WfbpClaimingAmountController.onSubmit(taxYear, businessId, NormalMode).url
  override def onwardRoute: Call = workingFromBusinessPremises.routes.LiveAtBusinessPremisesController.onPageLoad(taxYear, businessId, NormalMode)

  override def baseAnswers: UserAnswers = emptyUserAnswers.set(BusinessPremisesAmountPage, expensesAmount, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(bind[WorkplaceRunningCostsNavigator].toInstance(new FakeWorkplaceRunningCostsNavigator(onwardRoute)))

  def createForm(userType: UserType): Form[BigDecimal] = new WfbpClaimingAmountFormProvider()(userType, expensesAmount)

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[WfbpClaimingAmountView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }

}
