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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromHome

import base.questionPages.BooleanGetAndPostQuestionBaseSpec
import forms.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursFormProvider
import models.NormalMode
import models.common.UserType
import models.database.UserAnswers
import navigation.{ExpensesNavigator, FakeExpensesNavigator}
import pages.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursPage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.{Binding, bind}
import play.api.mvc.{Call, Request}
import views.html.journeys.expenses.workplaceRunningCosts.workingFromHome.MoreThan25HoursView

class MoreThan25HoursControllerSpec extends BooleanGetAndPostQuestionBaseSpec("MoreThan25HoursController", MoreThan25HoursPage) {

  override def onPageLoadCall: Call = routes.MoreThan25HoursController.onPageLoad(taxYear, businessId, NormalMode)
  override def onSubmitCall: Call   = routes.MoreThan25HoursController.onSubmit(taxYear, businessId, NormalMode)
  override def onwardRoute: Call    = routes.MoreThan25HoursController.onPageLoad(taxYear, businessId, NormalMode)

  override def pageAnswers: UserAnswers = baseAnswers.set(page, validAnswer, Some(businessId)).success.value

  override val bindings: List[Binding[_]] = List(
    bind[ExpensesNavigator].toInstance(new FakeExpensesNavigator(onwardRoute))
  )

  override def createForm(userType: UserType): Form[Boolean] = new MoreThan25HoursFormProvider()(userType)

  override def expectedView(expectedForm: Form[Boolean], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[MoreThan25HoursView]
    view(expectedForm, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId).toString()
  }
}
